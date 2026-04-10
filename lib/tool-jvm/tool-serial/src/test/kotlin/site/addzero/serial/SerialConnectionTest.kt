package site.addzero.serial

import java.io.ByteArrayOutputStream
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class SerialConnectionTest {
    @Test
    fun `read 会返回当前串口实际读到的文本内容`() {
        val driver =
            FakeSerialDriver(
                readChunks = listOf(
                    "TEMP=23.5".encodeToByteArray(),
                ),
            )
        val connection = SerialConnection(driver, SerialPortConfig(portName = "FAKE"))

        val bytes = connection.read(maxBytes = 64)
        val text = bytes.decodeToString()

        assertContentEquals("TEMP=23.5".encodeToByteArray(), bytes)
        assertEquals("TEMP=23.5", text)
    }

    @Test
    fun `轮询读取可以按日志行输出`() =
        runTest {
            val driver =
                FakeSerialDriver(
                    readChunks = listOf(
                        "booting...\nwi".encodeToByteArray(),
                        "fi connected\nready>\n".encodeToByteArray(),
                    ),
                )
            val connection = SerialConnection(driver, SerialPortConfig(portName = "FAKE"))

            val logs =
                connection
                    .pollLogLineFlow(
                        SerialLogStreamConfig(
                            pollIntervalMs = 10,
                        ),
                    )
                    .take(3)
                    .toList()

            assertEquals(
                listOf(
                    "booting...",
                    "wifi connected",
                    "ready>",
                ),
                logs,
            )
        }

    @Test
    fun `串口日志可以编码成 SSE 数据帧`() =
        runTest {
            val driver =
                FakeSerialDriver(
                    readChunks = listOf(
                        "line-1\nline-2\n".encodeToByteArray(),
                    ),
                )
            val connection = SerialConnection(driver, SerialPortConfig(portName = "FAKE"))

            val frames =
                connection
                    .pollSseLogFlow(
                        SerialSseStreamConfig(
                            logStream = SerialLogStreamConfig(pollIntervalMs = 10),
                            event = "device-log",
                        ),
                    ).take(2)
                    .toList()

            assertEquals(
                listOf(
                    "event: device-log\ndata: line-1\n\n",
                    "event: device-log\ndata: line-2\n\n",
                ),
                frames,
            )
        }

    @Test
    fun `readExact 会把多个分片拼成完整报文`() {
        /**
         * fake 驱动每次只吐一小段数据，
         * 用来模拟真实串口分片到达的情况。
         */
        val driver = FakeSerialDriver(readChunks = listOf("AB".encodeToByteArray(), "CD".encodeToByteArray()))
        val connection = SerialConnection(driver, SerialPortConfig(portName = "FAKE"))

        val bytes = connection.readExact(4)

        assertContentEquals("ABCD".encodeToByteArray(), bytes)
    }

    @Test
    fun `readUntil 会把分隔符一起返回`() {
        val driver =
            FakeSerialDriver(
                readChunks = listOf(
                    "HEL".encodeToByteArray(),
                    "LO\r".encodeToByteArray(),
                    "\nWORLD".encodeToByteArray(),
                ),
            )
        val connection = SerialConnection(driver, SerialPortConfig(portName = "FAKE"))

        val bytes = connection.readUntil("\r\n".encodeToByteArray())

        assertContentEquals("HELLO\r\n".encodeToByteArray(), bytes)
    }

    @Test
    fun `readUntil 在超时后会显式失败`() {
        val driver = FakeSerialDriver(readChunks = listOf("PING".encodeToByteArray()))
        val connection = SerialConnection(driver, SerialPortConfig(portName = "FAKE", readTimeoutMs = 20))

        assertFailsWith<SerialTimeoutException> {
            connection.readUntil("\r\n".encodeToByteArray())
        }
    }

    @Test
    fun `write 会把全部字节写到底层驱动`() {
        val driver = FakeSerialDriver()
        val connection = SerialConnection(driver, SerialPortConfig(portName = "FAKE"))

        connection.write("AT\r\n")

        assertEquals("AT\r\n", String(driver.written.toByteArray(), Charsets.UTF_8))
    }

    @Test
    fun `hexToByteArray 支持空格和前缀`() {
        val bytes = "0xAA 0xbb 01".hexToByteArray()

        assertContentEquals(byteArrayOf(0xAA.toByte(), 0xBB.toByte(), 0x01), bytes)
        assertEquals("AA BB 01", bytes.toHexString())
    }

    @Test
    fun `readAvailable 只读取当前缓冲区已有内容`() {
        val driver = FakeSerialDriver(readChunks = listOf("ABC".encodeToByteArray(), "DEF".encodeToByteArray()))
        val connection = SerialConnection(driver, SerialPortConfig(portName = "FAKE"))

        val bytes = connection.readAvailable(maxBytes = 3)

        assertContentEquals("ABC".encodeToByteArray(), bytes)
        assertTrue(driver.remainingBytes() > 0)
    }
}

private class FakeSerialDriver(
    readChunks: List<ByteArray> = emptyList(),
) : SerialDriver {
    /**
     * 预置待读取的数据队列。
     *
     * 每个元素代表底层驱动一次可能返回的一块数据。
     */
    private val queue = ArrayDeque(readChunks.map { it.copyOf() })

    /**
     * 收集测试过程中写入的数据，便于断言调用方最终发出了什么。
     */
    val written = ByteArrayOutputStream()

    override val systemPortName = "FAKE"
    override val isOpen = true

    override fun read(buffer: ByteArray): Int {
        val chunk = queue.removeFirstOrNull() ?: return 0
        val read = minOf(buffer.size, chunk.size)
        chunk.copyInto(buffer, endIndex = read)
        if (read < chunk.size) {
            /**
             * 如果调用方给的缓冲区比当前 chunk 小，
             * 把没读完的尾部重新塞回队列头部，模拟下次继续读。
             */
            queue.addFirst(chunk.copyOfRange(read, chunk.size))
        }
        return read
    }

    override fun write(buffer: ByteArray, offset: Int, length: Int): Int {
        written.write(buffer, offset, length)
        return length
    }

    override fun bytesAvailable(): Int = queue.firstOrNull()?.size ?: 0

    override fun flushIoBuffers(): Boolean {
        queue.clear()
        return true
    }

    override fun setDtr(enabled: Boolean) = Unit

    override fun setRts(enabled: Boolean) = Unit

    override fun close() = Unit

    /**
     * 统计 fake 驱动里还没被消费掉的总字节数。
     */
    fun remainingBytes(): Int = queue.sumOf { chunk -> chunk.size }
}
