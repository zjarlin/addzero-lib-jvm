package site.addzero.device.protocol.modbus.ksp.keil

import java.nio.file.Files
import kotlin.io.path.createDirectories
import kotlin.io.path.writeText
import kotlin.test.Test
import kotlin.test.assertContains
import site.addzero.device.protocol.modbus.ksp.core.ModbusTransportKind

class MxprojectSyncToolTest {
    private val tool = MxprojectSyncTool()

    @Test
    fun `updates mxproject cached header and source paths`() {
        val projectDir = Files.createTempDirectory("mxproject-sync-project")
        val baseDir = projectDir.resolve("MDK-ARM").apply { createDirectories() }
        val sourceA = projectDir.resolve("Core/Src/generated/modbus/rtu/device/device_generated.c").apply { parent.createDirectories() }
        val sourceB = projectDir.resolve("Core/Src/modbus/rtu/device/device_bridge_impl.c").apply { parent.createDirectories() }
        sourceA.writeText("void a(void) {}")
        sourceB.writeText("void b(void) {}")

        val original =
            """
            [PreviousUsedKeilFiles]
            SourceFiles=..\Core\Src\main.c
            HeaderPath=..\Core\Inc
            CDefines=USE_HAL_DRIVER;

            [PreviousGenFiles]
            HeaderFolderListSize=1
            HeaderPath#0=..\Core\Inc
            SourceFolderListSize=1
            SourcePath#0=..\Core\Src
            """.trimIndent()

        val updated =
            tool.updateMxprojectContent(
                original = original,
                transport = ModbusTransportKind.RTU,
                externalSourceFiles = listOf(sourceA.toFile(), sourceB.toFile()),
                baseDir = baseDir.toFile(),
            )

        assertContains(updated, "SourceFiles=..\\Core\\Src\\main.c;..\\Core\\Src\\generated\\modbus\\rtu\\device\\device_generated.c;..\\Core\\Src\\modbus\\rtu\\device\\device_bridge_impl.c")
        assertContains(updated, "HeaderPath=..\\Core\\Inc;..\\Core\\Inc\\generated\\modbus\\rtu")
        assertContains(updated, "HeaderFolderListSize=2")
        assertContains(updated, "HeaderPath#0=..\\Core\\Inc")
        assertContains(updated, "HeaderPath#1=..\\Core\\Inc\\generated\\modbus\\rtu")
        assertContains(updated, "SourceFolderListSize=3")
        assertContains(updated, "SourcePath#0=..\\Core\\Src")
        assertContains(updated, "SourcePath#1=..\\Core\\Src\\generated\\modbus\\rtu")
        assertContains(updated, "SourcePath#2=..\\Core\\Src\\modbus\\rtu")
    }
}
