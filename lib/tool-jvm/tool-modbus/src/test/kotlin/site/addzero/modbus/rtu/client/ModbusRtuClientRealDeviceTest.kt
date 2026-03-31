package site.addzero.modbus.rtu.client

import org.junit.jupiter.api.Test
import site.addzero.serial.SerialPortConfig
import java.io.File

private const val DEFAULT_REAL_PORT = "/dev/cu.usbserial-2140"
private const val REAL_UNIT_ID = 1
private const val REAL_ADDRESS = 0
private const val REAL_COUNT = 24
private const val REAL_BAUD_RATE = 9600
private const val REAL_TIMEOUT_MS = 1500

class ModbusRtuClientRealDeviceTest {
    @Test
    fun `真实设备读取 24 路线圈并打印结果`() {
        runRealModbusRead()
    }
}

fun main() {
    runRealModbusRead()
}

private fun runRealModbusRead() {
    val candidates = resolveCandidatePorts()
    val failures = linkedMapOf<String, String>()

    for (realPort in candidates) {
        try {
            val client =
                ModbusRtuClient(
                    ModbusRtuClientConfig(
                        serialConfig =
                            SerialPortConfig(
                                portName = realPort,
                                baudRate = REAL_BAUD_RATE,
                                readTimeoutMs = REAL_TIMEOUT_MS,
                                writeTimeoutMs = REAL_TIMEOUT_MS,
                            ),
                        unitId = REAL_UNIT_ID,
                        requestTimeoutMs = REAL_TIMEOUT_MS,
                    ),
                )

            client.use {
                val coils = it.readCoils(address = REAL_ADDRESS, count = REAL_COUNT)
                println(
                    "REAL_MODBUS_RTU_READ " +
                        "port=$realPort unitId=$REAL_UNIT_ID address=$REAL_ADDRESS " +
                        "count=$REAL_COUNT coils=$coils",
                )
                return
            }
        } catch (error: Throwable) {
            failures[realPort] = "${error::class.simpleName}: ${error.message}"
            println("REAL_MODBUS_RTU_READ_FAIL port=$realPort reason=${failures.getValue(realPort)}")
        }
    }

    error(
        "No real Modbus RTU port responded. " +
            failures.entries.joinToString(separator = "; ") { (port, reason) -> "[$port] $reason" },
    )
}

private fun resolveCandidatePorts(): List<String> {
    val overridePort = System.getProperty("modbus.real.port")
    if (!overridePort.isNullOrBlank()) {
        return listOf(overridePort)
    }

    val discovered =
        File("/dev")
            .listFiles()
            .orEmpty()
            .map { it.absolutePath }
            .filter { it.startsWith("/dev/cu.usbserial-") }
            .sorted()

    val preferred = listOf(DEFAULT_REAL_PORT, "/dev/cu.usbserial-2130")
    return (preferred + discovered).distinct()
}
