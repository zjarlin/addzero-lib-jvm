package site.addzero.serial

import java.nio.charset.Charset
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.isActive

/**
 * 串口文本日志轮询配置。
 *
 * 这一层面向“设备持续吐日志”的场景，
 * 例如 MCU 调试日志、模组状态输出、周期性状态上报。
 */
data class SerialLogStreamConfig(
    /**
     * 两次空轮询之间的等待时间。
     */
    val pollIntervalMs: Long = 100,
    /**
     * 单次尽量读取的最大字节数。
     */
    val maxChunkBytes: Int = DEFAULT_SERIAL_STREAM_CHUNK_BYTES,
    /**
     * 文本解码字符集。
     */
    val charset: Charset = Charsets.UTF_8,
    /**
     * 连接结束时是否把还没遇到换行的尾部文本也发出去。
     */
    val emitTrailingPartialLine: Boolean = true,
) {
    init {
        require(pollIntervalMs >= 0) {
            "pollIntervalMs 不能小于 0"
        }
        require(maxChunkBytes > 0) {
            "maxChunkBytes 必须大于 0"
        }
    }
}

/**
 * 串口 SSE 推流配置。
 *
 * 这层输出已经是标准 `text/event-stream` 文本帧，
 * controller 可以直接写回给浏览器，不需要再自己拼 `data:`。
 */
data class SerialSseStreamConfig(
    /**
     * 底层日志轮询配置。
     */
    val logStream: SerialLogStreamConfig = SerialLogStreamConfig(),
    /**
     * SSE 的事件名。
     */
    val event: String = "serial-log",
    /**
     * 心跳间隔。
     *
     * 大于 0 时，在长时间无新日志时会输出注释帧，
     * 便于网关、浏览器或代理维持长连接。
     */
    val heartbeatIntervalMs: Long = 15_000,
    /**
     * 心跳注释内容。
     */
    val heartbeatComment: String = "keep-alive",
) {
    init {
        require(heartbeatIntervalMs >= 0) {
            "heartbeatIntervalMs 不能小于 0"
        }
        require(event.isNotBlank()) {
            "event 不能为空"
        }
    }
}

/**
 * 按“日志行”轮询串口文本。
 *
 * 返回的是逐行文本流，而不是底层原始 chunk，
 * 这样更适合直接映射到日志面板、SSE、WebSocket 或控制台。
 */
fun SerialConnection.pollLogLineFlow(
    config: SerialLogStreamConfig = SerialLogStreamConfig(),
): Flow<String> =
    flow {
        val pending = StringBuilder()
        while (currentCoroutineContext().isActive && isOpen) {
            val bytes = readAvailable(config.maxChunkBytes)
            if (bytes.isEmpty()) {
                delay(config.pollIntervalMs)
                continue
            }

            pending.append(bytes.toString(config.charset))
            val completedLines = drainCompletedLines(pending)
            for (line in completedLines) {
                emit(line)
            }
        }

        if (config.emitTrailingPartialLine && pending.isNotEmpty()) {
            emit(pending.toString())
        }
    }

/**
 * 自动打开串口并以日志行形式持续输出。
 *
 * 适合 controller/service 不想自己管理串口生命周期的场景。
 */
fun SerialPortTool.openLogLineFlow(
    serialConfig: SerialPortConfig,
    streamConfig: SerialLogStreamConfig = SerialLogStreamConfig(),
): Flow<String> =
    flow {
        open(serialConfig).use { connection ->
            connection.pollLogLineFlow(streamConfig).collect { line ->
                emit(line)
            }
        }
    }

/**
 * 把串口日志转换成 SSE 文本帧。
 *
 * 返回值已经是完整的 `event:` / `data:` 格式，
 * controller 直接按 `text/event-stream` 输出即可。
 */
fun SerialConnection.pollSseLogFlow(
    config: SerialSseStreamConfig = SerialSseStreamConfig(),
): Flow<String> =
    pollLogLineFlow(config.logStream).transform { line ->
        emit(line.toSseFrame(event = config.event))
    }

/**
 * 自动打开串口并输出 SSE 文本帧。
 *
 * 这就是 controller 侧最直接可用的入口。
 */
fun SerialPortTool.openSseLogFlow(
    serialConfig: SerialPortConfig,
    streamConfig: SerialSseStreamConfig = SerialSseStreamConfig(),
): Flow<String> =
    flow {
        open(serialConfig).use { connection ->
            var lastEmissionTime = System.currentTimeMillis()
            val pending = StringBuilder()
            while (currentCoroutineContext().isActive && connection.isOpen) {
                val bytes = connection.readAvailable(streamConfig.logStream.maxChunkBytes)
                if (bytes.isNotEmpty()) {
                    pending.append(bytes.toString(streamConfig.logStream.charset))
                    val completedLines = drainCompletedLines(pending)
                    for (line in completedLines) {
                        lastEmissionTime = System.currentTimeMillis()
                        emit(line.toSseFrame(event = streamConfig.event))
                    }
                    continue
                }

                if (
                    streamConfig.heartbeatIntervalMs > 0 &&
                    System.currentTimeMillis() - lastEmissionTime >= streamConfig.heartbeatIntervalMs
                ) {
                    lastEmissionTime = System.currentTimeMillis()
                    emit(streamConfig.heartbeatComment.toSseCommentFrame())
                    continue
                }

                delay(streamConfig.logStream.pollIntervalMs)
            }

            if (streamConfig.logStream.emitTrailingPartialLine && pending.isNotEmpty()) {
                emit(pending.toString().toSseFrame(event = streamConfig.event))
            }
        }
    }

/**
 * 把普通文本编码成 SSE 数据帧。
 */
fun String.toSseFrame(
    event: String? = null,
): String {
    val normalized = replace("\r\n", "\n").replace('\r', '\n')
    val out = StringBuilder()
    if (!event.isNullOrBlank()) {
        out.append("event: ").append(event).append('\n')
    }
    normalized.split('\n').forEach { line ->
        out.append("data: ").append(line).append('\n')
    }
    out.append('\n')
    return out.toString()
}

/**
 * 生成 SSE 注释帧。
 */
fun String.toSseCommentFrame(): String = ": $this\n\n"

/**
 * 从待处理缓冲区里切出完整日志行。
 *
 * 这里统一兼容 `\n` 和 `\r\n`，
 * 并保留日志正文，不把换行符带给上层。
 */
private fun drainCompletedLines(
    pending: StringBuilder,
): List<String> {
    val completedLines = mutableListOf<String>()
    while (true) {
        val newlineIndex = pending.indexOf("\n")
        if (newlineIndex < 0) {
            return completedLines
        }
        val rawLine = pending.substring(0, newlineIndex)
        val normalizedLine = rawLine.removeSuffix("\r")
        completedLines += normalizedLine
        pending.delete(0, newlineIndex + 1)
    }
}

/**
 * 串口日志流默认读取块大小。
 */
private const val DEFAULT_SERIAL_STREAM_CHUNK_BYTES = 4096
