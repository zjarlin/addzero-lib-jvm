package site.addzero.modbus.tcp.client

import java.net.ServerSocket
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import site.addzero.modbus.ModbusProtocolException
import site.addzero.modbus.tcp.server.ModbusTcpServer
import site.addzero.modbus.tcp.server.ModbusTcpServerConfig

class ModbusTcpClientTest {
    /**
     * 记录当前测试启动的服务端，方便在 `tearDown` 里统一关闭。
     */
    private var server: ModbusTcpServer? = null

    @AfterTest
    fun tearDown() {
        server?.close()
        server = null
    }

    @Test
    fun `客户端可以读取服务端预置的点位`() {
        val port = freeTcpPort()
        val startedServer =
            ModbusTcpServer(
                ModbusTcpServerConfig(
                    host = "127.0.0.1",
                    port = port,
                    defaultUnitId = 1,
                ),
            )
        startedServer.image().apply {
            setCoils(0, listOf(true, false, true))
            setDiscreteInputs(10, listOf(false, true))
            setHoldingRegisters(20, listOf(100, 200, 65535))
            setInputRegisters(30, listOf(7, 8))
        }
        startedServer.start()
        server = startedServer

        ModbusTcpClient(
            ModbusTcpClientConfig(
                host = "127.0.0.1",
                port = port,
                unitId = 1,
            ),
        ).use { client ->
            assertContentEquals(listOf(true, false, true), client.readCoils(0, 3))
            assertContentEquals(listOf(false, true), client.readDiscreteInputs(10, 2))
            assertContentEquals(listOf(100, 200, 65535), client.readHoldingRegisters(20, 3))
            assertContentEquals(listOf(7, 8), client.readInputRegisters(30, 2))
        }
    }

    @Test
    fun `客户端写入后服务端映像会同步更新`() {
        val port = freeTcpPort()
        val startedServer =
            ModbusTcpServer(
                ModbusTcpServerConfig(
                    host = "127.0.0.1",
                    port = port,
                    defaultUnitId = 1,
                ),
            )
        startedServer.image().apply {
            setCoils(0, listOf(false, false, false))
            setHoldingRegisters(10, listOf(0, 0, 0))
        }
        startedServer.start()
        server = startedServer

        ModbusTcpClient(
            ModbusTcpClientConfig(
                host = "127.0.0.1",
                port = port,
                unitId = 1,
            ),
        ).use { client ->
            client.writeSingleCoil(0, true)
            client.writeMultipleCoils(1, listOf(true, false))
            client.writeSingleRegister(10, 321)
            client.writeMultipleRegisters(11, listOf(654, 987))
        }

        val image = startedServer.image()
        assertTrue(image.getCoil(0))
        assertTrue(image.getCoil(1))
        assertFalse(image.getCoil(2))
        assertEquals(321, image.getHoldingRegister(10))
        assertEquals(654, image.getHoldingRegister(11))
        assertEquals(987, image.getHoldingRegister(12))
    }

    @Test
    fun `读取不存在地址时应抛出结构化协议异常`() {
        val port = freeTcpPort()
        val startedServer =
            ModbusTcpServer(
                ModbusTcpServerConfig(
                    host = "127.0.0.1",
                    port = port,
                    defaultUnitId = 1,
                ),
            )
        startedServer.image().setHoldingRegister(0, 1)
        startedServer.start()
        server = startedServer

        ModbusTcpClient(
            ModbusTcpClientConfig(
                host = "127.0.0.1",
                port = port,
                unitId = 1,
            ),
        ).use { client ->
            val error = assertFailsWith<ModbusProtocolException> {
                client.readHoldingRegister(100)
            }

            assertEquals(0x03, error.functionCode)
            assertEquals(2, error.exceptionCode)
            assertEquals("Illegal Data Address", error.exceptionName)
        }
    }
}

private fun freeTcpPort(): Int =
    /**
     * 通过临时占用 0 端口的方式向操作系统申请一个空闲端口，
     * 用来降低并发测试时的端口冲突概率。
     */
    ServerSocket(0).use { socket ->
        socket.localPort
    }
