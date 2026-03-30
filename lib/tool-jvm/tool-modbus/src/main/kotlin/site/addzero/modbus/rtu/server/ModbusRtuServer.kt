package site.addzero.modbus.rtu.server

import com.fazecast.jSerialComm.SerialPort
import com.ghgande.j2mod.modbus.Modbus
import com.ghgande.j2mod.modbus.procimg.SimpleProcessImage
import com.ghgande.j2mod.modbus.slave.ModbusSlave
import com.ghgande.j2mod.modbus.slave.ModbusSlaveFactory
import com.ghgande.j2mod.modbus.util.SerialParameters
import java.io.Closeable
import java.util.concurrent.ConcurrentHashMap
import site.addzero.modbus.ModbusProcessImage
import site.addzero.modbus.ModbusToolException
import site.addzero.serial.SerialFlowControl
import site.addzero.serial.SerialParity
import site.addzero.serial.SerialPortConfig
import site.addzero.serial.SerialStopBits

/**
 * Kotlin 风格的 Modbus RTU 服务端。
 *
 * 这里的“服务端”对应的是串口从站：
 * - 监听某个串口
 * - 根据 unit id 暴露 process image
 * - 接收外部主站对 coil / register 的读写
 */
class ModbusRtuServer private constructor(
    val config: ModbusRtuServerConfig,
    private val slaveFactory: ModbusRtuSlaveFactory,
) : Closeable {
    private val lock = Any()
    private val images = ConcurrentHashMap<Int, ModbusProcessImage>()

    @Volatile
    private var slave: ModbusRtuSlaveBinding? = null

    constructor(config: ModbusRtuServerConfig) : this(config, J2modModbusRtuSlaveFactory)

    internal constructor(
        config: ModbusRtuServerConfig,
        slaveFactory: ModbusRtuSlaveFactory,
        marker: Boolean = true,
    ) : this(config, slaveFactory)

    val isRunning: Boolean
        get() = slave != null

    /**
     * 返回指定 unit id 的 process image；不存在时自动创建。
     */
    fun image(unitId: Int = config.defaultUnitId): ModbusProcessImage {
        require(unitId in 0..255) {
            "unitId 必须在 0..255"
        }
        val image =
            images.computeIfAbsent(unitId) {
                ModbusProcessImage(
                    unitId = unitId,
                    delegate = SimpleProcessImage(unitId),
                )
            }
        slave?.addProcessImage(unitId, image.delegate)
        return image
    }

    /**
     * 打开 RTU 从站监听。
     */
    fun start() {
        synchronized(lock) {
            if (slave != null) {
                return
            }
            val created =
                try {
                    slaveFactory.create(config)
                } catch (throwable: Throwable) {
                    throw ModbusToolException("启动 Modbus RTU 服务端失败：port=${config.serialConfig.portName}", throwable)
                }
            val existingImages =
                if (images.isEmpty()) {
                    listOf(image(config.defaultUnitId))
                } else {
                    images.values.toList()
                }
            existingImages.forEach { processImage ->
                created.addProcessImage(processImage.unitId, processImage.delegate)
            }
            try {
                created.open()
            } catch (throwable: Throwable) {
                runCatching { created.close() }
                throw ModbusToolException("打开 Modbus RTU 串口监听失败：port=${config.serialConfig.portName}", throwable)
            }
            slave = created
        }
    }

    override fun close() {
        synchronized(lock) {
            val current = slave ?: return
            runCatching { current.close() }
            slave = null
        }
    }
}

internal fun interface ModbusRtuSlaveFactory {
    fun create(config: ModbusRtuServerConfig): ModbusRtuSlaveBinding
}

internal interface ModbusRtuSlaveBinding : Closeable {
    fun addProcessImage(unitId: Int, image: SimpleProcessImage)

    fun open()
}

internal object J2modModbusRtuSlaveFactory : ModbusRtuSlaveFactory {
    override fun create(config: ModbusRtuServerConfig): ModbusRtuSlaveBinding {
        val slave = ModbusSlaveFactory.createSerialSlave(createRtuSerialParameters(config.serialConfig))
        return J2modModbusRtuSlaveBinding(slave)
    }
}

private class J2modModbusRtuSlaveBinding(
    private val slave: ModbusSlave,
) : ModbusRtuSlaveBinding {
    override fun addProcessImage(unitId: Int, image: SimpleProcessImage) {
        slave.addProcessImage(unitId, image)
    }

    override fun open() {
        slave.open()
    }

    override fun close() {
        ModbusSlaveFactory.close(slave)
    }
}

internal fun createRtuSerialParameters(config: SerialPortConfig): SerialParameters =
    SerialParameters(
        config.portName,
        config.baudRate,
        config.flowControl.toJSerialCommInput(),
        config.flowControl.toJSerialCommOutput(),
        config.dataBits,
        config.stopBits.toJSerialComm(),
        config.parity.toJSerialComm(),
        false,
    ).apply {
        encoding = Modbus.SERIAL_ENCODING_RTU
        openDelay = config.openSafetySleepTimeMs
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

private fun SerialFlowControl.toJSerialCommInput(): Int =
    when (this) {
        SerialFlowControl.NONE -> SerialPort.FLOW_CONTROL_DISABLED
        SerialFlowControl.RTS_CTS -> SerialPort.FLOW_CONTROL_CTS_ENABLED
        SerialFlowControl.DTR_DSR -> SerialPort.FLOW_CONTROL_DSR_ENABLED
        SerialFlowControl.XON_XOFF -> SerialPort.FLOW_CONTROL_XONXOFF_IN_ENABLED
    }

private fun SerialFlowControl.toJSerialCommOutput(): Int =
    when (this) {
        SerialFlowControl.NONE -> SerialPort.FLOW_CONTROL_DISABLED
        SerialFlowControl.RTS_CTS -> SerialPort.FLOW_CONTROL_RTS_ENABLED
        SerialFlowControl.DTR_DSR -> SerialPort.FLOW_CONTROL_DTR_ENABLED
        SerialFlowControl.XON_XOFF -> SerialPort.FLOW_CONTROL_XONXOFF_OUT_ENABLED
    }
