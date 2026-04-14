package site.addzero.serial

import java.io.ByteArrayOutputStream
import java.io.Closeable
import java.nio.charset.Charset

/**
 * 打开的串口连接。
 *
 * 一个 [SerialConnection] 对应一个已经成功打开的底层串口句柄。
 * 它负责：
 * - 顺序写入字节流
 * - 按不同策略读取返回数据
 * - 管理缓冲区与关闭动作
 *
 * 它不负责：
 * - 协议编码解码
 * - 自动重试
 * - 请求响应匹配
 *
 * 这些应留给更上层的协议库，例如 Modbus RTU、AT 命令封装或厂商私有协议实现。
 */
class SerialConnection internal constructor(
    /**
     * 真正负责和操作系统串口交互的底层驱动。
     *
     * 这里故意不直接暴露 `jSerialComm`，这样测试时可以替换成 fake。
     */
    private val driver: SerialDriver,
    /**
     * 打开当前连接时使用的串口参数。
     *
     * 这个配置是“当时怎么打开的”，不是可变运行态配置。
     */
    val config: SerialPortConfig,
) : Closeable {
    /**
     * 当前底层驱动识别到的系统串口名。
     *
     * 这个值一般会和 [SerialPortConfig.portName] 接近，
     * 但仍以驱动最终打开的端口信息为准。
     */
    val systemPortName
        get() = driver.systemPortName

    /**
     * 当前连接是否仍处于打开状态。
     */
    val isOpen
        get() = driver.isOpen

    /**
     * 写入完整字节数组；如果底层只写出部分数据，会继续补写直到完成或失败。
     *
     * 适用于已经明确知道完整报文内容的场景，例如：
     * - 写一帧 Modbus RTU 请求
     * - 写一条以 `\r\n` 结尾的 AT 指令
     * - 写厂商定义的二进制命令
     */
    fun write(bytes: ByteArray) {
        require(isOpen) {
            "串口尚未打开：$systemPortName"
        }
        if (bytes.isEmpty()) {
            return
        }

        var offset = 0
        while (offset < bytes.size) {
            /**
             * 某些驱动一次不一定能把剩余数据全部写完，
             * 所以这里必须循环补写，直到整帧写完为止。
             */
            val written = driver.write(bytes, offset, bytes.size - offset)
            if (written <= 0) {
                throw SerialTimeoutException("串口写入超时：port=$systemPortName expected=${bytes.size} written=$offset")
            }
            offset += written
        }
    }

    /**
     * 以指定字符集写入文本。
     *
     * 文本最终仍会转换为字节数组发送，
     * 因此如果你的协议本身是二进制帧，优先直接调用 [write]。
     */
    fun write(text: String, charset: Charset = Charsets.UTF_8) {
        write(text.toByteArray(charset))
    }

    /**
     * 单次读取最多 [maxBytes] 字节。
     *
     * 如果在配置的读超时内没有收到任何字节，返回空数组而不是抛错，
     * 方便做轮询式读取。
     *
     * 适合“先看看现在有没有数据”的场景，不适合严格的定长协议收包。
     */
    fun read(maxBytes: Int = DEFAULT_READ_CHUNK_SIZE): ByteArray {
        require(maxBytes > 0) {
            "maxBytes 必须大于 0"
        }
        val buffer = ByteArray(maxBytes)
        val read = driver.read(buffer)
        if (read <= 0) {
            return ByteArray(0)
        }
        return buffer.copyOf(read)
    }

    /**
     * 精确读取 [size] 字节；如果读超时仍未凑够，直接失败。
     *
     * 适用于协议头已经告诉你 payload 长度的场景。
     */
    fun readExact(size: Int): ByteArray {
        require(size > 0) {
            "size 必须大于 0"
        }
        val out = ByteArrayOutputStream(size)
        while (out.size() < size) {
            val remaining = size - out.size()
            val chunk = ByteArray(remaining)
            /**
             * 这里每次只申请“还差多少字节”的缓冲区，
             * 这样逻辑最直观，也方便报错时展示当前已拼出的长度。
             */
            val read = driver.read(chunk)
            if (read <= 0) {
                throw SerialTimeoutException(
                    "串口读取超时：port=$systemPortName expected=$size actual=${out.size()} timeoutMs=${config.readTimeoutMs}",
                )
            }
            out.write(chunk, 0, read)
        }
        return out.toByteArray()
    }

    /**
     * 读取直到出现 [delimiter]。
     *
     * 默认会把分隔符本身一起包含到返回值里。
     *
     * 适用于以固定结束符收包的文本协议或半文本协议，
     * 例如以 `\r\n` 作为报文结束标记的设备响应。
     */
    fun readUntil(
        delimiter: ByteArray,
        maxBytes: Int = DEFAULT_MAX_PACKET_SIZE,
        includeDelimiter: Boolean = true,
    ): ByteArray {
        require(delimiter.isNotEmpty()) {
            "delimiter 不能为空"
        }
        require(maxBytes > 0) {
            "maxBytes 必须大于 0"
        }

        val out = ByteArrayOutputStream()
        while (out.size() < maxBytes) {
            val remaining = maxBytes - out.size()
            /**
             * 分隔符模式下不知道一次会读到多少数据，
             * 因此按固定 chunk 逐步累积，避免单次申请过大缓冲区。
             */
            val nextChunkSize = remaining.coerceAtMost(DEFAULT_READ_CHUNK_SIZE)
            val chunk = ByteArray(nextChunkSize)
            val read = driver.read(chunk)
            if (read <= 0) {
                throw SerialTimeoutException(
                    "串口读取超时：port=$systemPortName delimiter=${delimiter.toHexString()} bytes=${out.size()} timeoutMs=${config.readTimeoutMs}",
                )
            }
            out.write(chunk, 0, read)
            val current = out.toByteArray()
            /**
             * 分隔符可能跨 chunk 边界出现，
             * 所以不能只在本次 `chunk` 里找，必须在累计结果里找。
             */
            val delimiterIndex = current.indexOfSequence(delimiter)
            if (delimiterIndex >= 0) {
                val endExclusive =
                    if (includeDelimiter) {
                        delimiterIndex + delimiter.size
                    } else {
                        delimiterIndex
                    }
                return current.copyOf(endExclusive)
            }
        }

        throw SerialPortException(
            "在 maxBytes=$maxBytes 内未找到分隔符：port=$systemPortName delimiter=${delimiter.toHexString()}",
        )
    }

    /**
     * 立即把当前驱动缓冲区里可读的数据尽量读出来，不额外等待。
     *
     * 与 [read] 的区别是：
     * - [read] 允许底层在超时范围内阻塞等待
     * - 这个方法只消费“现在已经到了”的数据
     */
    fun readAvailable(maxBytes: Int = DEFAULT_MAX_PACKET_SIZE): ByteArray {
        require(maxBytes > 0) {
            "maxBytes 必须大于 0"
        }
        val out = ByteArrayOutputStream()
        while (out.size() < maxBytes) {
            /**
             * 先看驱动当前说“已经到达”的字节数，
             * 再决定本轮最多读多少，避免不必要的阻塞等待。
             */
            val available = driver.bytesAvailable().coerceAtLeast(0)
            if (available <= 0) {
                break
            }
            val nextSize = minOf(available, maxBytes - out.size())
            val chunk = ByteArray(nextSize)
            val read = driver.read(chunk)
            if (read <= 0) {
                break
            }
            out.write(chunk, 0, read)
        }
        return out.toByteArray()
    }

    /**
     * 清空驱动层的收发缓冲区。
     *
     * 一般在这些场景下有用：
     * - 刚打开串口，先丢掉设备上电残留数据
     * - 上一次协议交互失败后，手动丢弃脏数据
     * - 测试前重置收发状态
     */
    fun clearBuffers() {
        require(driver.flushIoBuffers()) {
            "串口缓冲区清空失败：$systemPortName"
        }
    }

    /**
     * 显式切换 DTR 线路状态。
     */
    fun setDtr(enabled: Boolean) {
        require(isOpen) {
            "串口尚未打开：$systemPortName"
        }
        driver.setDtr(enabled)
    }

    /**
     * 显式切换 RTS 线路状态。
     */
    fun setRts(enabled: Boolean) {
        require(isOpen) {
            "串口尚未打开：$systemPortName"
        }
        driver.setRts(enabled)
    }

    override fun close() {
        driver.close()
    }

    companion object {
        /**
         * 普通读取场景下的默认单次拉取块大小。
         *
         * 这个值不是协议限制，只是折中后的默认读取粒度。
         */
        private const val DEFAULT_READ_CHUNK_SIZE = 256

        /**
         * 基础工具层允许读取的默认最大报文大小。
         *
         * 如果你的设备响应明显更大，调用方应显式传更高的 `maxBytes`。
         */
        private const val DEFAULT_MAX_PACKET_SIZE = 4096
    }
}

/**
 * 在当前字节数组里查找目标连续片段第一次出现的位置。
 *
 * 这个实现是线性扫描，足够应付串口短报文场景。
 */
private fun ByteArray.indexOfSequence(target: ByteArray): Int {
    if (target.isEmpty() || size < target.size) {
        return -1
    }
    val lastStart = size - target.size
    for (start in 0..lastStart) {
        var matched = true
        for (offset in target.indices) {
            /**
             * 只要某一位不匹配，就立刻结束本次起点尝试，
             * 然后继续尝试下一个起点。
             */
            if (this[start + offset] != target[offset]) {
                matched = false
                break
            }
        }
        if (matched) {
            return start
        }
    }
    return -1
}
