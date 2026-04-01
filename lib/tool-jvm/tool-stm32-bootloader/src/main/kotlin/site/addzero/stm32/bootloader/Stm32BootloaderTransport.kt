package site.addzero.stm32.bootloader

import java.io.Closeable
import site.addzero.serial.SerialConnection
import site.addzero.serial.SerialPortTool

internal interface Stm32BootloaderTransport : Closeable {
    fun write(bytes: ByteArray)

    fun readExact(size: Int): ByteArray

    fun clearBuffers()

    fun setLine(line: Stm32ControlLine, output: Boolean)
}

internal fun interface Stm32BootloaderTransportFactory {
    fun open(config: Stm32BootloaderConfig): Stm32BootloaderTransport
}

internal object SerialBootloaderTransportFactory : Stm32BootloaderTransportFactory {
    override fun open(config: Stm32BootloaderConfig): Stm32BootloaderTransport {
        return SerialBootloaderTransport(SerialPortTool.open(config.serialConfig))
    }
}

internal class SerialBootloaderTransport(
    private val connection: SerialConnection,
) : Stm32BootloaderTransport {
    override fun write(bytes: ByteArray) {
        connection.write(bytes)
    }

    override fun readExact(size: Int): ByteArray {
        return connection.readExact(size)
    }

    override fun clearBuffers() {
        connection.clearBuffers()
    }

    override fun setLine(line: Stm32ControlLine, output: Boolean) {
        when (line) {
            Stm32ControlLine.DTR -> connection.setDtr(output)
            Stm32ControlLine.RTS -> connection.setRts(output)
        }
    }

    override fun close() {
        connection.close()
    }
}

internal fun interface Stm32BootloaderSleeper {
    fun sleep(durationMs: Long)
}

internal object ThreadBootloaderSleeper : Stm32BootloaderSleeper {
    override fun sleep(durationMs: Long) {
        if (durationMs > 0) {
            Thread.sleep(durationMs)
        }
    }
}
