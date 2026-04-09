package site.addzero.device.driver.modbus.tcp

import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertSame
import site.addzero.modbus.ModbusProtocolException

class J2modModbusTcpExecutorTest {
    @Test
    fun `maps endpoint config to tool client config and delegates reads`() = runBlocking {
        val executor = J2modModbusTcpExecutor { endpoint ->
            val config = endpoint.toClientConfig()
            FakeToolModbusTcpClient(
                onReadHoldingRegisters = { address, quantity ->
                    assertEquals("127.0.0.1", config.host)
                    assertEquals(1502, config.port)
                    assertEquals(3, config.unitId)
                    assertEquals(800, config.timeoutMs)
                    assertEquals(10, address)
                    assertEquals(2, quantity)
                    listOf(11, 12)
                },
            )
        }

        val result = executor.readHoldingRegisters(
            ModbusTcpEndpointConfig(
                serviceId = "tcp-demo",
                host = "127.0.0.1",
                port = 1502,
                unitId = 3,
                timeoutMs = 800,
                retries = 1,
            ),
            address = 10,
            quantity = 2,
        )

        assertEquals(listOf(11, 12), result)
    }

    @Test
    fun `wraps tcp tool failures with endpoint context`() = runBlocking {
        val executor = J2modModbusTcpExecutor {
            FakeToolModbusTcpClient(
                onWriteSingleCoil = { _, _ ->
                    error("offline")
                },
            )
        }

        val error = assertFailsWith<IllegalStateException> {
            executor.writeSingleCoil(
                ModbusTcpEndpointConfig(
                    serviceId = "tcp-svc",
                    host = "192.168.1.10",
                    port = 502,
                    unitId = 1,
                    timeoutMs = 1000,
                    retries = 0,
                ),
                address = 2,
                value = true,
            )
        }

        assertEquals(true, error.message?.contains("host=192.168.1.10:502"))
    }

    @Test
    fun `preserves tcp structured modbus tool exceptions`() = runBlocking {
        val protocolError =
            ModbusProtocolException(
                message = "device busy",
                functionCode = 0x05,
                exceptionCode = 6,
                exceptionName = "Slave Device Busy",
            )
        val executor = J2modModbusTcpExecutor {
            FakeToolModbusTcpClient(
                onWriteSingleCoil = { _, _ ->
                    throw protocolError
                },
            )
        }

        val error = assertFailsWith<ModbusProtocolException> {
            executor.writeSingleCoil(
                ModbusTcpEndpointConfig(
                    serviceId = "tcp-svc",
                    host = "192.168.1.10",
                    port = 502,
                    unitId = 1,
                    timeoutMs = 1000,
                    retries = 0,
                ),
                address = 2,
                value = true,
            )
        }

        assertSame(protocolError, error)
    }
}

private class FakeToolModbusTcpClient(
    private val onReadCoils: (Int, Int) -> List<Boolean> = { _, _ -> emptyList() },
    private val onReadDiscreteInputs: (Int, Int) -> List<Boolean> = { _, _ -> emptyList() },
    private val onReadHoldingRegisters: (Int, Int) -> List<Int> = { _, _ -> emptyList() },
    private val onReadInputRegisters: (Int, Int) -> List<Int> = { _, _ -> emptyList() },
    private val onWriteSingleCoil: (Int, Boolean) -> Unit = { _, _ -> },
    private val onWriteMultipleCoils: (Int, List<Boolean>) -> Unit = { _, _ -> },
    private val onWriteSingleRegister: (Int, Int) -> Unit = { _, _ -> },
    private val onWriteMultipleRegisters: (Int, List<Int>) -> Unit = { _, _ -> },
) : ToolModbusTcpClient {
    override fun readCoils(address: Int, quantity: Int): List<Boolean> = onReadCoils(address, quantity)

    override fun readDiscreteInputs(address: Int, quantity: Int): List<Boolean> = onReadDiscreteInputs(address, quantity)

    override fun readHoldingRegisters(address: Int, quantity: Int): List<Int> = onReadHoldingRegisters(address, quantity)

    override fun readInputRegisters(address: Int, quantity: Int): List<Int> = onReadInputRegisters(address, quantity)

    override fun writeSingleCoil(address: Int, value: Boolean) = onWriteSingleCoil(address, value)

    override fun writeMultipleCoils(address: Int, values: List<Boolean>) = onWriteMultipleCoils(address, values)

    override fun writeSingleRegister(address: Int, value: Int) = onWriteSingleRegister(address, value)

    override fun writeMultipleRegisters(address: Int, values: List<Int>) = onWriteMultipleRegisters(address, values)

    override fun close() = Unit
}
