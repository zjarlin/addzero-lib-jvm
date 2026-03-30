package site.addzero.serial

import com.fazecast.jSerialComm.SerialPort

/**
 * 串口底层驱动抽象。
 *
 * 主逻辑依赖这个接口，测试里可以用假实现替换，
 * 避免单元测试必须绑定真实串口设备。
 */
internal interface SerialDriver {
    val systemPortName: String
    val isOpen: Boolean

    fun read(buffer: ByteArray): Int

    fun write(buffer: ByteArray, offset: Int, length: Int): Int

    fun bytesAvailable(): Int

    fun flushIoBuffers(): Boolean

    fun close()
}

/**
 * 基于 jSerialComm 的默认驱动实现。
 *
 * 这里是串口库与本项目 API 的唯一适配层：
 * - 上层只看见 [SerialConnection] / [SerialPortConfig]
 * - 底层 `jSerialComm` 细节都收口在这里
 */
internal class JSerialCommDriver(
    private val serialPort: SerialPort,
    private val config: SerialPortConfig,
) : SerialDriver {
    override val systemPortName: String
        get() = serialPort.systemPortName

    override val isOpen: Boolean
        get() = serialPort.isOpen

    init {
        /**
         * `jSerialComm` 的打开流程是：
         * 1. 先把端口参数和超时参数写进去
         * 2. 再真正打开端口
         *
         * 这样做可以避免“先以默认参数打开，再立刻改参数”带来的短暂错误状态。
         */
        serialPort.setComPortParameters(
            config.baudRate,
            config.dataBits,
            config.stopBits.toJSerialComm(),
            config.parity.toJSerialComm(),
        )
        serialPort.setFlowControl(config.flowControl.toJSerialComm())
        serialPort.setComPortTimeouts(
            SerialPort.TIMEOUT_READ_SEMI_BLOCKING or SerialPort.TIMEOUT_WRITE_BLOCKING,
            config.readTimeoutMs,
            config.writeTimeoutMs,
        )
        val opened = serialPort.openPort(config.openSafetySleepTimeMs)
        if (!opened) {
            throw SerialPortException("串口打开失败：port=${config.portName}")
        }
    }

    override fun read(buffer: ByteArray): Int {
        if (buffer.isEmpty()) {
            return 0
        }
        val read = serialPort.readBytes(buffer, buffer.size)
        if (read == -1) {
            throw SerialPortException("串口读取失败：port=${config.portName}")
        }
        return read
    }

    override fun write(buffer: ByteArray, offset: Int, length: Int): Int {
        if (length == 0) {
            return 0
        }
        val written = serialPort.writeBytes(buffer, length, offset)
        return when (written) {
            -1 -> throw SerialPortException("串口写入失败：port=${config.portName}")
            -2 -> throw SerialPortException(
                "串口写入参数非法：port=${config.portName} offset=$offset length=$length buffer=${buffer.size}",
            )

            else -> written
        }
    }

    override fun bytesAvailable(): Int = serialPort.bytesAvailable()

    override fun flushIoBuffers(): Boolean = serialPort.flushIOBuffers()

    override fun close() {
        if (!serialPort.closePort() && serialPort.isOpen) {
            throw SerialPortException("串口关闭失败：port=${config.portName}")
        }
    }
}

private fun SerialStopBits.toJSerialComm(): Int =
    when (this) {
        SerialStopBits.ONE -> SerialPort.ONE_STOP_BIT
        SerialStopBits.ONE_POINT_FIVE -> SerialPort.ONE_POINT_FIVE_STOP_BITS
        SerialStopBits.TWO -> SerialPort.TWO_STOP_BITS
    }

private fun SerialParity.toJSerialComm(): Int =
    when (this) {
        SerialParity.NONE -> SerialPort.NO_PARITY
        SerialParity.EVEN -> SerialPort.EVEN_PARITY
        SerialParity.ODD -> SerialPort.ODD_PARITY
        SerialParity.MARK -> SerialPort.MARK_PARITY
        SerialParity.SPACE -> SerialPort.SPACE_PARITY
    }

private fun SerialFlowControl.toJSerialComm(): Int =
    when (this) {
        SerialFlowControl.NONE -> SerialPort.FLOW_CONTROL_DISABLED
        SerialFlowControl.RTS_CTS -> SerialPort.FLOW_CONTROL_RTS_ENABLED or SerialPort.FLOW_CONTROL_CTS_ENABLED
        SerialFlowControl.DTR_DSR -> SerialPort.FLOW_CONTROL_DTR_ENABLED or SerialPort.FLOW_CONTROL_DSR_ENABLED
        SerialFlowControl.XON_XOFF ->
            SerialPort.FLOW_CONTROL_XONXOFF_IN_ENABLED or SerialPort.FLOW_CONTROL_XONXOFF_OUT_ENABLED
    }
