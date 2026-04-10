package site.addzero.serial

import java.nio.file.Files
import java.nio.file.Paths
import kotlin.test.Test
import kotlin.test.assertTrue
import org.junit.jupiter.api.Assumptions.assumeTrue

class SerialPortToolRealDeviceTest {
    @Test
    fun `写死配置读取真实板卡日志`() {
        val portPath = "/dev/cu.usbserial-2140"
        assumeTrue(Files.exists(Paths.get(portPath)), "真实串口不存在：$portPath")

        val config =
            SerialPortConfig(
                portName = portPath,
                baudRate = 115200,
                dataBits = 8,
                stopBits = SerialStopBits.ONE,
                parity = SerialParity.NONE,
                flowControl = SerialFlowControl.NONE,
                readTimeoutMs = 200,
            )

        val collected = StringBuilder()

        SerialPortTool.open(config).use { connection ->
            val deadline = System.currentTimeMillis() + 5_000
            while (System.currentTimeMillis() < deadline) {
                val bytes = connection.readAvailable()
                if (bytes.isEmpty()) {
                    Thread.sleep(100)
                    continue
                }
                collected.append(bytes.decodeToString())
            }
        }

        println("REAL_SERIAL_LOG_BEGIN")
        println(collected.toString())
        println("REAL_SERIAL_LOG_END")

        assertTrue(
            actual = collected.isNotEmpty(),
            message = "5 秒内没有从 $portPath 读到任何日志",
        )
    }
}
