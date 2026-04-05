package site.addzero.device.driver.modbus.rtu

import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class J2modModbusRtuExecutorTest {
    @Test
    fun `maps endpoint config to tool client config and delegates reads`() = runBlocking {
        val executor = J2modModbusRtuExecutor { endpoint ->
            val config = endpoint.toClientConfig()
            FakeToolModbusRtuClient(
                onReadCoils = { address, quantity ->
                    assertEquals("/dev/ttyUSB9", config.serialConfig.portName)
                    assertEquals(115200, config.serialConfig.baudRate)
                    assertEquals(2, config.retries)
                    assertEquals(960, config.requestTimeoutMs)
                    assertEquals(4, address)
                    assertEquals(2, quantity)
                    listOf(true, false)
                },
            )
        }

        val result = executor.readCoils(
            DefaultModbusRtuEndpointConfig(
                portPath = "/dev/ttyUSB9",
                unitId = 7,
                baudRate = 115200,
                dataBits = 8,
                stopBits = 2,
                parity = ModbusSerialParity.EVEN,
                timeoutMs = 960,
                retries = 2,
            ),
            address = 4,
            quantity = 2,
        )

        assertEquals(listOf(1, 0), result)
    }

    @Test
    fun `wraps tool failures with endpoint context`() = runBlocking {
        val executor = J2modModbusRtuExecutor {
            FakeToolModbusRtuClient(
                onWriteSingleRegister = { _, _ ->
                    error("boom")
                },
            )
        }

        val error = assertFailsWith<IllegalStateException> {
            executor.writeSingleRegister(
                DefaultModbusRtuEndpointConfig(
                    portPath = "COM7",
                    unitId = 1,
                    baudRate = 9600,
                    timeoutMs = 1000,
                    retries = 0,
                ),
                address = 3,
                value = 12,
            )
        }

        assertEquals(true, error.message?.contains("port=COM7"))
        assertEquals(false, error.message?.contains("service="))
    }
}

private class FakeToolModbusRtuClient(
    private val onReadCoils: (Int, Int) -> List<Boolean> = { _, _ -> emptyList() },
    private val onReadDiscreteInputs: (Int, Int) -> List<Boolean> = { _, _ -> emptyList() },
    private val onReadHoldingRegisters: (Int, Int) -> List<Int> = { _, _ -> emptyList() },
    private val onReadInputRegisters: (Int, Int) -> List<Int> = { _, _ -> emptyList() },
    private val onWriteSingleCoil: (Int, Boolean) -> Unit = { _, _ -> },
    private val onWriteMultipleCoils: (Int, List<Boolean>) -> Unit = { _, _ -> },
    private val onWriteSingleRegister: (Int, Int) -> Unit = { _, _ -> },
    private val onWriteMultipleRegisters: (Int, List<Int>) -> Unit = { _, _ -> },
) : ToolModbusRtuClient {
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
