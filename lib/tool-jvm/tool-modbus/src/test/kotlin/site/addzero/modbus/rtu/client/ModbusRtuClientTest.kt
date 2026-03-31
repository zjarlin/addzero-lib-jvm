package site.addzero.modbus.rtu.client

import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import site.addzero.modbus.ModbusToolException
import site.addzero.serial.SerialPortConfig

class ModbusRtuClientTest {
    /**
     * 收集测试期间打开过的 fake 会话，
     * 用来断言重连次数和关闭行为。
     */
    private val openedSessions = mutableListOf<FakeRtuSession>()
    private var failFirstOpen = false

    private val sessionFactory =
        ModbusRtuSessionFactory {
            /**
             * 通过开关控制“第一次打开失败”，
             * 用来验证客户端的重试逻辑。
             */
            if (failFirstOpen) {
                failFirstOpen = false
                throw IllegalStateException("open failed")
            }
            FakeRtuSession().also { session -> openedSessions += session }
        }

    @AfterTest
    fun tearDown() {
        openedSessions.clear()
        failFirstOpen = false
    }

    @Test
    fun `客户端可以通过会话读取和写入 RTU 数据`() {
      val config = defaultClientConfig()
      val client = ModbusRtuClient(config, sessionFactory)
        client.connect()
        val session = openedSessions.single()
        session.coils = listOf(true, false, true)
        session.discreteInputs = listOf(false, true)
        session.holdingRegisters = listOf(11, 22)
        session.inputRegisters = listOf(33)

        assertContentEquals(listOf(true, false, true), client.readCoils(0, 3))
        assertContentEquals(listOf(false, true), client.readDiscreteInputs(10, 2))
        assertContentEquals(listOf(11, 22), client.readHoldingRegisters(20, 2))
        assertContentEquals(listOf(33), client.readInputRegisters(30, 1))

        client.writeSingleCoil(1, true)
        client.writeMultipleCoils(2, listOf(false, true))
        client.writeSingleRegister(3, 123)
        client.writeMultipleRegisters(4, listOf(456, 789))

        assertEquals(Triple(1, 1, true), session.singleCoilWrite)
        assertEquals(Triple(1, 2, listOf(false, true)), session.multiCoilWrite)
        assertEquals(Triple(1, 3, 123), session.singleRegisterWrite)
        assertEquals(Triple(1, 4, listOf(456, 789)), session.multiRegisterWrite)
    }

    @Test
    fun `按请求重连模式会为每次调用重新创建会话`() {
        val client =
            ModbusRtuClient(
                defaultClientConfig().copy(reconnectPerRequest = true),
                sessionFactory,
            )
        openedSessions += FakeRtuSession()
        openedSessions.clear()

        client.readCoil(0)
        client.readCoil(1)

        assertEquals(2, openedSessions.size)
        assertTrue(openedSessions.all { session -> session.closed })
    }

    @Test
    fun `超过重试次数后会抛出统一异常`() {
        val failingFactory =
            ModbusRtuSessionFactory {
                throw IllegalStateException("always failed")
            }
        val client =
            ModbusRtuClient(
                defaultClientConfig().copy(retries = 1),
                failingFactory,
            )

        assertFailsWith<ModbusToolException> {
            client.readCoil(0)
        }
    }

    private fun defaultClientConfig(): ModbusRtuClientConfig =
        ModbusRtuClientConfig(
            serialConfig =
                SerialPortConfig(
                    portName = "/dev/ttyUSB0",
                    baudRate = 9600,
                ),
        )
}

private class FakeRtuSession : ModbusRtuSession {
    /**
     * 下面这些字段分别模拟设备当前能读到的数据区。
     */
    var coils: List<Boolean> = listOf(false)
    var discreteInputs: List<Boolean> = listOf(false)
    var holdingRegisters: List<Int> = listOf(0)
    var inputRegisters: List<Int> = listOf(0)
    var singleCoilWrite: Triple<Int, Int, Boolean>? = null
    var multiCoilWrite: Triple<Int, Int, List<Boolean>>? = null
    var singleRegisterWrite: Triple<Int, Int, Int>? = null
    var multiRegisterWrite: Triple<Int, Int, List<Int>>? = null
    var closed: Boolean = false

    override fun readCoils(unitId: Int, address: Int, count: Int): List<Boolean> = coils.take(count)

    override fun readDiscreteInputs(unitId: Int, address: Int, count: Int): List<Boolean> = discreteInputs.take(count)

    override fun readHoldingRegisters(unitId: Int, address: Int, count: Int): List<Int> = holdingRegisters.take(count)

    override fun readInputRegisters(unitId: Int, address: Int, count: Int): List<Int> = inputRegisters.take(count)

    override fun writeSingleCoil(unitId: Int, address: Int, value: Boolean) {
        singleCoilWrite = Triple(unitId, address, value)
    }

    override fun writeMultipleCoils(unitId: Int, address: Int, values: List<Boolean>) {
        multiCoilWrite = Triple(unitId, address, values)
    }

    override fun writeSingleRegister(unitId: Int, address: Int, value: Int) {
        singleRegisterWrite = Triple(unitId, address, value)
    }

    override fun writeMultipleRegisters(unitId: Int, address: Int, values: List<Int>) {
        multiRegisterWrite = Triple(unitId, address, values)
    }

    override fun close() {
        closed = true
    }
}
