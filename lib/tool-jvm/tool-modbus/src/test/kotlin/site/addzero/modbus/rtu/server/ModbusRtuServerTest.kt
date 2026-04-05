package site.addzero.modbus.rtu.server

import com.ghgande.j2mod.modbus.procimg.SimpleProcessImage
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import site.addzero.serial.SerialPortConfig

class ModbusRtuServerTest {
    /**
     * 收集测试里创建出的 fake 从站绑定对象，
     * 用来验证映像挂载和关闭行为。
     */
    private val bindings = mutableListOf<FakeRtuSlaveBinding>()

    private val slaveFactory =
        ModbusRtuSlaveFactory { _ ->
            FakeRtuSlaveBinding().also { binding -> bindings += binding }
        }

    @AfterTest
    fun tearDown() {
        bindings.clear()
    }

    @Test
    fun `服务端启动时会挂载默认 unit process image`() {
        val server = ModbusRtuServer(defaultServerConfig(), slaveFactory)

        server.start()

        assertTrue(server.isRunning)
        assertEquals(1, bindings.size)
        assertTrue(bindings.single().opened)
        assertTrue(bindings.single().images.containsKey(1))
    }

    @Test
    fun `服务端会把预置的多个 unit image 挂载到底层从站`() {
        val server = ModbusRtuServer(defaultServerConfig().copy(defaultUnitId = 2), slaveFactory)
        server.image(2).setHoldingRegister(0, 100)
        server.image(3).setHoldingRegister(1, 200)

        server.start()

        val binding = bindings.single()
        assertEquals(2, binding.images.size)
        assertEquals(100, binding.images.getValue(2).getRegister(0).value and 0xFFFF)
        assertEquals(200, binding.images.getValue(3).getRegister(1).value and 0xFFFF)
    }

    @Test
    fun `关闭服务端后底层从站会被关闭`() {
        val server = ModbusRtuServer(defaultServerConfig(), slaveFactory)
        server.start()

        server.close()

        assertFalse(server.isRunning)
        assertTrue(bindings.single().closed)
    }

    private fun defaultServerConfig(): ModbusRtuServerConfig =
        ModbusRtuServerConfig(
            serialConfig =
                SerialPortConfig(
                    portName = "/dev/ttyUSB0",
                    baudRate = 9600,
                ),
        )
}

private class FakeRtuSlaveBinding : ModbusRtuSlaveBinding {
    /**
     * 保存每个 unit id 挂进去的底层 process image，
     * 相当于 fake 从站内部的寄存器仓库。
     */
    val images = linkedMapOf<Int, SimpleProcessImage>()
    var opened = false
    var closed = false

    override fun addProcessImage(unitId: Int, image: SimpleProcessImage) {
        images[unitId] = image
    }

    override fun open() {
        opened = true
    }

    override fun close() {
        closed = true
    }
}
