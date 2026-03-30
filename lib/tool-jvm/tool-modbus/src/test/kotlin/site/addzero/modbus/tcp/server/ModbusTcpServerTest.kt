package site.addzero.modbus.tcp.server

import com.ghgande.j2mod.modbus.facade.ModbusTCPMaster
import com.ghgande.j2mod.modbus.procimg.SimpleRegister
import java.net.ServerSocket
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class ModbusTcpServerTest {
    private var server: ModbusTcpServer? = null
    private var externalMaster: ModbusTCPMaster? = null

    @AfterTest
    fun tearDown() {
        runCatching {
            externalMaster?.disconnect()
        }
        externalMaster = null
        server?.close()
        server = null
    }

    @Test
    fun `服务端可以向外暴露预置寄存器映像`() {
        val port = freeTcpPort()
        val startedServer =
            ModbusTcpServer(
                ModbusTcpServerConfig(
                    host = "127.0.0.1",
                    port = port,
                    defaultUnitId = 2,
                ),
            )
        startedServer.image(2).apply {
            setCoils(0, listOf(true, true, false))
            setHoldingRegisters(5, listOf(11, 22))
            setInputRegisters(8, listOf(33))
        }
        startedServer.start()
        server = startedServer

        val master = ModbusTCPMaster("127.0.0.1", port, 1_000, false)
        externalMaster = master
        master.connect()

        val coils = List(3) { index -> master.readCoils(2, 0, 3).getBit(index) }
        val holding = master.readMultipleRegisters(2, 5, 2).map { register -> register.value and 0xFFFF }
        val input = master.readInputRegisters(2, 8, 1).map { register -> register.value and 0xFFFF }

        assertContentEquals(listOf(true, true, false), coils)
        assertContentEquals(listOf(11, 22), holding)
        assertContentEquals(listOf(33), input)
    }

    @Test
    fun `外部主站写入后服务端映像可以直接读到最新值`() {
        val port = freeTcpPort()
        val startedServer =
            ModbusTcpServer(
                ModbusTcpServerConfig(
                    host = "127.0.0.1",
                    port = port,
                    defaultUnitId = 3,
                ),
            )
        startedServer.image(3).apply {
            setCoils(0, listOf(false, false))
            setHoldingRegisters(10, listOf(0, 0))
        }
        startedServer.start()
        server = startedServer

        val master = ModbusTCPMaster("127.0.0.1", port, 1_000, false)
        externalMaster = master
        master.connect()
        master.writeCoil(3, 0, true)
        master.writeMultipleRegisters(3, 10, arrayOf(SimpleRegister(444), SimpleRegister(555)))

        val image = startedServer.image(3)
        assertEquals(true, image.getCoil(0))
        assertEquals(444, image.getHoldingRegister(10))
        assertEquals(555, image.getHoldingRegister(11))
    }
}

private fun freeTcpPort(): Int =
    ServerSocket(0).use { socket ->
        socket.localPort
    }
