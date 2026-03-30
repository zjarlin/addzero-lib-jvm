package site.addzero.modbus.tcp.server

import com.ghgande.j2mod.modbus.procimg.SimpleProcessImage
import com.ghgande.j2mod.modbus.slave.ModbusSlave
import com.ghgande.j2mod.modbus.slave.ModbusSlaveFactory
import java.io.Closeable
import java.net.InetAddress
import java.util.concurrent.ConcurrentHashMap
import site.addzero.modbus.ModbusProcessImage
import site.addzero.modbus.ModbusToolException

/**
 * Kotlin 风格的 Modbus TCP 服务端。
 *
 * 该类把 j2mod 的 `ModbusSlave + SimpleProcessImage` 组合收敛成更直接的 API，
 * 便于业务代码和测试代码以寄存器映像的方式操作从站数据。
 */
class ModbusTcpServer(
    val config: ModbusTcpServerConfig,
) : Closeable {
    private val lock = Any()
    private val images = ConcurrentHashMap<Int, ModbusProcessImage>()

    @Volatile
    private var slave: ModbusSlave? = null

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
     * 启动 TCP 从站监听。
     */
    fun start() {
        synchronized(lock) {
            if (slave != null) {
                return
            }
            val created =
                runModbus("启动 Modbus TCP 服务端失败：port=${config.port}") {
                    ModbusSlaveFactory.createTCPSlave(
                        resolveHost(config.host),
                        config.port,
                        config.workerPoolSize,
                        config.useRtuOverTcp,
                        config.maxIdleSeconds,
                    )
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
            runModbus("打开 Modbus TCP 服务端监听失败：port=${config.port}") {
                created.open()
            }
            slave = created
        }
    }

    /**
     * 关闭服务端监听并释放 j2mod 持有的从站对象。
     */
    override fun close() {
        synchronized(lock) {
            val current = slave ?: return
            runCatching {
                ModbusSlaveFactory.close(current)
            }
            slave = null
        }
    }

    private fun resolveHost(host: String?): InetAddress? =
        host?.let { value -> InetAddress.getByName(value) }

    private fun <T> runModbus(message: String, block: () -> T): T =
        try {
            block()
        } catch (throwable: Throwable) {
            throw ModbusToolException(message, throwable)
        }
}
