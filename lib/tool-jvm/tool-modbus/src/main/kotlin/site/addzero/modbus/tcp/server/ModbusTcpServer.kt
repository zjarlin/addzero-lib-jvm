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
    /**
     * 当前服务端监听参数。
     */
    val config: ModbusTcpServerConfig,
) : Closeable {
    /**
     * 启停锁，避免并发 start/close 导致底层从站状态混乱。
     */
    private val lock = Any()

    /**
     * 维护 unit id 到寄存器映像的对应关系。
     */
    private val images = ConcurrentHashMap<Int, ModbusProcessImage>()

    @Volatile
    private var slave: ModbusSlave? = null

    val isRunning
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
                /**
                 * 第一次访问某个 unit id 时才真正创建底层映像，
                 * 避免无用地址占用内存。
                 */
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
                    /**
                     * 如果调用方还没手动准备任何映像，
                     * 至少自动挂一个默认 unit，避免服务端启动后完全没有可访问对象。
                     */
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
        /**
         * j2mod 允许传 null 表示监听所有地址，
         * 所以这里只在非空时才做 DNS/字面量地址解析。
         */
        host?.let { value -> InetAddress.getByName(value) }

    private fun <T> runModbus(message: String, block: () -> T): T =
        try {
            block()
        } catch (throwable: Throwable) {
            throw ModbusToolException(message, throwable)
        }
}
