package site.addzero.serial

import java.io.ByteArrayOutputStream
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class SerialConnectionTest {
    @Test
    fun `readExact 会把多个分片拼成完整报文`() {
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
    private val queue = ArrayDeque(readChunks.map { it.copyOf() })
    val written = ByteArrayOutputStream()

    override val systemPortName: String = "FAKE"
    override val isOpen: Boolean = true

    override fun read(buffer: ByteArray): Int {
        val chunk = queue.removeFirstOrNull() ?: return 0
        val read = minOf(buffer.size, chunk.size)
        chunk.copyInto(buffer, endIndex = read)
        if (read < chunk.size) {
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

    override fun close() = Unit

    fun remainingBytes(): Int = queue.sumOf { chunk -> chunk.size }
}
