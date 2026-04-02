import site.addzero.stm32.bootloader.Stm32BootloaderConfig
import site.addzero.stm32.bootloader.Stm32BootloaderLineControl
import site.addzero.stm32.bootloader.Stm32ControlLine
import site.addzero.stm32.bootloader.Stm32ControlSignal
import site.addzero.stm32.bootloader.Stm32UartBootloader
import site.addzero.stm32.bootloader.stm32BootloaderSerialConfig
import site.addzero.serial.SerialParity

data class MappingCandidate(
    val boot0: Stm32ControlSignal?,
    val reset: Stm32ControlSignal?,
    val label: String,
)

val portName = args.firstOrNull() ?: error("需要传串口名，例如 /dev/cu.usbserial-2130")

val candidates = buildList {
    add(
        MappingCandidate(
            boot0 = null,
            reset = null,
            label = "no-line-control",
        ),
    )

    val lines = listOf(Stm32ControlLine.DTR, Stm32ControlLine.RTS)
    for (bootLine in lines) {
        for (resetLine in lines) {
            if (bootLine == resetLine) {
                continue
            }
            for (bootAsserted in listOf(true, false)) {
                for (resetAsserted in listOf(true, false)) {
                    add(
                        MappingCandidate(
                            boot0 = Stm32ControlSignal(bootLine, assertedOutput = bootAsserted),
                            reset = Stm32ControlSignal(resetLine, assertedOutput = resetAsserted),
                            label = "boot0=$bootLine/$bootAsserted reset=$resetLine/$resetAsserted",
                        ),
                    )
                }
            }
        }
    }
}

println("开始探测端口: $portName")
println("候选线控方案数量: ${candidates.size}")

val baudRates = listOf(115200, 57600, 38400, 19200, 9600)
val parities = listOf(SerialParity.EVEN, SerialParity.NONE)
println("波特率候选: ${baudRates.joinToString()}")
println("校验位候选: ${parities.joinToString()}")

data class ProbeSuccess(
    val candidate: MappingCandidate,
    val baudRate: Int,
    val parity: SerialParity,
)

var success: ProbeSuccess? = null

var attempt = 0
val totalAttempts = candidates.size * baudRates.size * parities.size

for (candidate in candidates) {
    for (baudRate in baudRates) {
        for (parity in parities) {
            attempt += 1
            println("")
            println("[$attempt/$totalAttempts] 尝试 ${candidate.label} baud=$baudRate parity=$parity")
            val config = Stm32BootloaderConfig(
                serialConfig = stm32BootloaderSerialConfig(
                    portName = portName,
                    baudRate = baudRate,
                    parity = parity,
                ),
                autoEnterBootloaderOnConnect = candidate.boot0 != null && candidate.reset != null,
                lineControl =
                    if (candidate.boot0 != null && candidate.reset != null) {
                        Stm32BootloaderLineControl(
                            boot0 = candidate.boot0,
                            reset = candidate.reset,
                            boot0SetupDelayMs = 30,
                            resetPulseMs = 60,
                            bootloaderReadyDelayMs = 120,
                            flashBootReadyDelayMs = 120,
                        )
                    } else {
                        null
                    },
                connectAttempts = 1,
                connectRetryDelayMs = 50,
            )

            runCatching {
                Stm32UartBootloader(config).use { bootloader ->
                    val info = bootloader.connect()
                    println("连接成功")
                    println("protocolVersion=${info.protocolVersionText}")
                    println("bootloaderVersion=0x${info.bootloaderVersion.toString(16).uppercase()}")
                    println("chipId=0x${info.chipId.toString(16).uppercase()}")
                    println("supportedCommands=${info.supportedCommands.joinToString { it.name }}")
                    if (candidate.reset != null) {
                        println("测试 resetTarget()")
                        bootloader.resetTarget()
                        println("resetTarget() 已执行")
                    }
                    if (candidate.boot0 != null && candidate.reset != null) {
                        println("测试 bootFromFlash()")
                        bootloader.bootFromFlash()
                        println("bootFromFlash() 已执行")
                    }
                    success = ProbeSuccess(candidate, baudRate, parity)
                }
            }.onFailure { throwable ->
                println("失败: ${throwable::class.simpleName}: ${throwable.message}")
            }

            if (success != null) {
                break
            }
        }
        if (success != null) {
            break
        }
    }
    if (success != null) {
        break
    }
}

println("")
if (success == null) {
    println("未找到可用的 Bootloader 连接方案")
} else {
    println(
        "找到可用方案: ${success!!.candidate.label}, baud=${success!!.baudRate}, parity=${success!!.parity}",
    )
}
