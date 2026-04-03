#!/usr/bin/env kotlin

import site.addzero.stm32.bootloader.StLinkConfig
import site.addzero.stm32.bootloader.Stm32EraseMode
import site.addzero.stm32.bootloader.Stm32FlashRequest
import site.addzero.stm32.bootloader.Stm32StLinkFlashGeometry
import site.addzero.stm32.bootloader.Stm32StLinkProgrammer

fun Long.hex(): String = "0x" + toString(16).uppercase()

Stm32StLinkProgrammer(
    StLinkConfig(
        connectUnderReset = true,
    ),
).use { programmer ->
    val target = programmer.connectTarget()
    val flashSizeReg = programmer.readMemory32(0x1FFFF7E0, 16)
    val flashSizeKilobytes = (flashSizeReg[0].toInt() and 0xFF) or ((flashSizeReg[1].toInt() and 0xFF) shl 8)
    val flashSizeBytes = if (flashSizeKilobytes > 0) flashSizeKilobytes * 1024 else 256 * 1024
    val geometry =
        Stm32StLinkFlashGeometry(
            flashBaseAddress = 0x08000000,
            flashSizeBytes = flashSizeBytes,
            pageSizeBytes = 0x800,
            sramBaseAddress = 0x20000000,
            sramSizeBytes = 0x10000,
        )
    val lastPageAddress = geometry.flashBaseAddress + geometry.flashSizeBytes - geometry.pageSizeBytes
    val lastPageIndex = ((lastPageAddress - geometry.flashBaseAddress) / geometry.pageSizeBytes).toInt()
    println("Target chipId=${target.chipId.toLong().hex()} flash=${geometry.flashSizeBytes} page=${geometry.pageSizeBytes}")
    println("Restoring last page index=$lastPageIndex address=${lastPageAddress.hex()}")
    val original = programmer.readMemory32(lastPageAddress, geometry.pageSizeBytes)
    val report =
        programmer.flash(
            request =
                Stm32FlashRequest(
                    startAddress = lastPageAddress,
                    firmware = original,
                    eraseMode = Stm32EraseMode.PageCodes(listOf(lastPageIndex)),
                    verifyAfterWrite = true,
                    startApplicationAfterWrite = true,
                ),
            targetInfo = target,
            geometry = geometry,
            progressListener = { progress ->
                val total = if (progress.stageTotalBytes <= 0) "-" else progress.stageTotalBytes.toString()
                println(
                    "[${progress.stage}] ${"%.1f".format(progress.overallPercent)}% " +
                        "stage=${progress.stageCompletedBytes}/$total ${progress.message}",
                )
            },
        )
    println("Flash done: bytes=${report.bytesWritten} verified=${report.verified} started=${report.startedApplication}")
    println("Extra pulse reset...")
    programmer.pulseReset()
    println("Pulse reset done")
}
