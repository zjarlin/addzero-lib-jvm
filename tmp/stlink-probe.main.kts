#!/usr/bin/env kotlin

import site.addzero.stm32.bootloader.StLinkConfig
import site.addzero.stm32.bootloader.Stm32StLinkProgrammer

fun Long.hex(): String = "0x" + toString(16).uppercase()

println("Listing probes...")
val probes = Stm32StLinkProgrammer.listProbes()
println("Probes: $probes")

require(probes.isNotEmpty()) {
    "No ST-Link probe found"
}

val config = StLinkConfig(
    connectUnderReset = true,
)

Stm32StLinkProgrammer(config).use { programmer ->
    println("Opened probe: ${programmer.probeInfo}")
    val version = programmer.readVersion()
    println("Version: ${version.versionText} pid=${version.productId.toString(16)}")
    println("Mode before attach: ${programmer.currentMode()}")
    println("Target voltage: ${programmer.readTargetVoltageMillivolts()} mV")

    val target = programmer.connectTarget()
    println("Target attached:")
    println("  coreId=${target.coreId.hex()}")
    println("  cpuId=${target.cpuId.hex()}")
    println("  chipId=${target.chipId.toLong().hex()}")
    println("  chipIdRegister=${target.chipIdRegisterAddress.hex()}")
    println("  mode=${target.currentMode}")
    println("  voltage=${target.targetVoltageMillivolts} mV")
    println("Probe flash size register region...")
    val flashSizeReg = programmer.readMemory32(0x1FFFF7E0, 16)
    println("  0x1FFFF7E0=" + flashSizeReg.joinToString(" ") { each -> "%02X".format(each.toInt() and 0xFF) })
    println("Probe flash edge...")
    val flashEdge = programmer.readMemory32(0x0807FFF0, 16)
    println("  0x0807FFF0=" + flashEdge.joinToString(" ") { each -> "%02X".format(each.toInt() and 0xFF) })

    println("Pulse reset...")
    programmer.pulseReset()
    println("Pulse reset done")

    println("System reset...")
    programmer.resetSystem()
    println("System reset done")
}
