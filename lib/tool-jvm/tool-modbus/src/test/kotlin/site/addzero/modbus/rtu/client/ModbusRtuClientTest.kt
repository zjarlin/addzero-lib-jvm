package site.addzero.modbus.rtu.client

import com.ghgande.j2mod.modbus.ModbusSlaveException
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import site.addzero.modbus.ModbusProtocolException
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
    fun `上位机访问下位机时应作为 RTU client 并读取从站 1 的 24 路线圈`() {
        val client =
            ModbusRtuClient(
                defaultClientConfig(
                    portName = "/dev/cu.usbserial-2140",
                    unitId = 1,
                ),
                sessionFactory,
            )
        client.connect()
        val session = openedSessions.single()
        session.setCoils(
            0,
            listOf(
                true, false, true, false, true, false,
                true, false, true, false, true, false,
                true, false, true, false, true, false,
                true, false, true, false, true, false,
            ),
        )

        val actual = client.readCoils(address = 0, count = 24)

        assertEquals(Triple(1, 0, 24), session.lastReadCoils)
        assertEquals(24, actual.size)
        assertTrue(actual[0])
        assertEquals(false, actual[1])
        assertTrue(actual[22])
        assertEquals(false, actual[23])
    }

    @Test
    fun `客户端可以通过会话读写不同类型的 RTU 数据`() {
        val client = ModbusRtuClient(defaultClientConfig(), sessionFactory)
        client.connect()
        val session = openedSessions.single()
        session.setCoils(0, listOf(true, false, true))
        session.setDiscreteInputs(10, listOf(false, true))
        session.setHoldingRegisters(20, listOf(11, 22))
        session.setInputRegisters(30, listOf(33))

        assertContentEquals(listOf(true, false, true), client.readCoils(0, 3))
        assertEquals(Triple(1, 0, 3), session.lastReadCoils)
        assertContentEquals(listOf(false, true), client.readDiscreteInputs(10, 2))
        assertEquals(Triple(1, 10, 2), session.lastReadDiscreteInputs)
        assertContentEquals(listOf(11, 22), client.readHoldingRegisters(20, 2))
        assertEquals(Triple(1, 20, 2), session.lastReadHoldingRegisters)
        assertContentEquals(listOf(33), client.readInputRegisters(30, 1))
        assertEquals(Triple(1, 30, 1), session.lastReadInputRegisters)

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

    @Test
    fun `从站返回异常码时应抛出结构化协议异常`() {
        val client =
            ModbusRtuClient(
                defaultClientConfig().copy(retries = 0, reconnectPerRequest = true),
                ModbusRtuSessionFactory {
                    object : ModbusRtuSession {
                        override fun readCoils(unitId: Int, address: Int, count: Int): List<Boolean> = emptyList()

                        override fun readDiscreteInputs(unitId: Int, address: Int, count: Int): List<Boolean> = emptyList()

                        override fun readHoldingRegisters(unitId: Int, address: Int, count: Int): List<Int> {
                            throw ModbusSlaveException(2)
                        }

                        override fun readInputRegisters(unitId: Int, address: Int, count: Int): List<Int> = emptyList()

                        override fun writeSingleCoil(unitId: Int, address: Int, value: Boolean) = Unit

                        override fun writeMultipleCoils(unitId: Int, address: Int, values: List<Boolean>) = Unit

                        override fun writeSingleRegister(unitId: Int, address: Int, value: Int) = Unit

                        override fun writeMultipleRegisters(unitId: Int, address: Int, values: List<Int>) = Unit

                        override fun close() = Unit
                    }
                },
            )

        val error = assertFailsWith<ModbusProtocolException> {
            client.readHoldingRegister(10)
        }

        assertEquals(0x03, error.functionCode)
        assertEquals(2, error.exceptionCode)
        assertEquals("Illegal Data Address", error.exceptionName)
    }

    private fun defaultClientConfig(): ModbusRtuClientConfig =
        defaultClientConfig(portName = "/dev/cu.usbserial-2140")

    private fun defaultClientConfig(
        portName: String,
        unitId: Int = 1,
    ): ModbusRtuClientConfig =
        ModbusRtuClientConfig(
            serialConfig =
                SerialPortConfig(
                    portName = portName,
                    baudRate = 9600,
                ),
            unitId = unitId,
        )
}

private class FakeRtuSession : ModbusRtuSession {
    /**
     * 下面这些字段分别模拟设备当前能读到的数据区。
     */
    private val coils = mutableMapOf<Int, Boolean>()
    private val discreteInputs = mutableMapOf<Int, Boolean>()
    private val holdingRegisters = mutableMapOf<Int, Int>()
    private val inputRegisters = mutableMapOf<Int, Int>()
    var lastReadCoils: Triple<Int, Int, Int>? = null
    var lastReadDiscreteInputs: Triple<Int, Int, Int>? = null
    var lastReadHoldingRegisters: Triple<Int, Int, Int>? = null
    var lastReadInputRegisters: Triple<Int, Int, Int>? = null
    var singleCoilWrite: Triple<Int, Int, Boolean>? = null
    var multiCoilWrite: Triple<Int, Int, List<Boolean>>? = null
    var singleRegisterWrite: Triple<Int, Int, Int>? = null
    var multiRegisterWrite: Triple<Int, Int, List<Int>>? = null
    var closed = false

    fun setCoils(address: Int, values: List<Boolean>) {
        values.forEachIndexed { index, value ->
            coils[address + index] = value
        }
    }

    fun setDiscreteInputs(address: Int, values: List<Boolean>) {
        values.forEachIndexed { index, value ->
            discreteInputs[address + index] = value
        }
    }

    fun setHoldingRegisters(address: Int, values: List<Int>) {
        values.forEachIndexed { index, value ->
            holdingRegisters[address + index] = value
        }
    }

    fun setInputRegisters(address: Int, values: List<Int>) {
        values.forEachIndexed { index, value ->
            inputRegisters[address + index] = value
        }
    }

    override fun readCoils(unitId: Int, address: Int, count: Int): List<Boolean> {
        lastReadCoils = Triple(unitId, address, count)
        return List(count) { offset -> coils[address + offset] ?: false }
    }

    override fun readDiscreteInputs(unitId: Int, address: Int, count: Int): List<Boolean> {
        lastReadDiscreteInputs = Triple(unitId, address, count)
        return List(count) { offset -> discreteInputs[address + offset] ?: false }
    }

    override fun readHoldingRegisters(unitId: Int, address: Int, count: Int): List<Int> {
        lastReadHoldingRegisters = Triple(unitId, address, count)
        return List(count) { offset -> holdingRegisters[address + offset] ?: 0 }
    }

    override fun readInputRegisters(unitId: Int, address: Int, count: Int): List<Int> {
        lastReadInputRegisters = Triple(unitId, address, count)
        return List(count) { offset -> inputRegisters[address + offset] ?: 0 }
    }

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
