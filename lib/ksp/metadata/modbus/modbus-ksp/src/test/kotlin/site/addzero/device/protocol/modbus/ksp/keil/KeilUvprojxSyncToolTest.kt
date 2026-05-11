package site.addzero.device.protocol.modbus.ksp.keil

import java.nio.file.Files
import kotlin.io.path.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.writeText
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import site.addzero.device.protocol.modbus.ksp.core.ModbusTransportKind

class KeilUvprojxSyncToolTest {
    private val tool = KeilUvprojxSyncTool()

    @Test
    fun `replaces only the configured group inside the configured target`() {
        val projectDir = Files.createTempDirectory("keil-sync-project")
        val uvprojxPath = projectDir.resolve("MDK-ARM/test1.uvprojx").apply { parent.createDirectories() }
        val sourceA = projectDir.resolve("Core/Src/generated/modbus/rtu/device/device_generated.c").apply { parent.createDirectories() }
        val sourceB = projectDir.resolve("Core/Src/modbus/rtu/device/device_bridge_impl.c").apply { parent.createDirectories() }
        val sourceC = projectDir.resolve("Core/Src/generated/modbus/rtu/transport/modbus_rtu_dispatch.c").apply { parent.createDirectories() }
        sourceA.writeText("void a(void) {}")
        sourceB.writeText("void b(void) {}")
        sourceC.writeText("void c(void) {}")
        val original =
            """
            <Project>
              <Targets>
                <Target>
                  <TargetName>bootloader</TargetName>
                  <Groups>
                    <Group>
                      <GroupName>Core/modbus/rtu</GroupName>
                      <Files>
                        <File><FileName>legacy_boot.c</FileName><FileType>1</FileType><FilePath>..\Boot\legacy_boot.c</FilePath></File>
                      </Files>
                    </Group>
                  </Groups>
                </Target>
                <Target>
                  <TargetName>test1</TargetName>
                  <Groups>
                    <Group>
                      <GroupName>Core</GroupName>
                      <Files>
                        <File><FileName>main.c</FileName><FileType>1</FileType><FilePath>..\Core\Src\main.c</FilePath></File>
                      </Files>
                    </Group>
                    <Group>
                      <GroupName>Core/modbus/rtu</GroupName>
                      <Files>
                        <File><FileName>legacy_modbus.c</FileName><FileType>1</FileType><FilePath>..\Core\Src\legacy_modbus.c</FilePath></File>
                      </Files>
                    </Group>
                    <Group>
                      <GroupName>Core/modbus/rtu/transport</GroupName>
                      <Files>
                        <File><FileName>legacy_transport.c</FileName><FileType>1</FileType><FilePath>..\Core\Src\legacy_transport.c</FilePath></File>
                      </Files>
                    </Group>
                    <Group>
                      <GroupName>Core/modbus/device</GroupName>
                      <Files>
                        <File><FileName>legacy_device.c</FileName><FileType>1</FileType><FilePath>..\Core\Src\modbus\device\legacy_device.c</FilePath></File>
                      </Files>
                    </Group>
                    <Group>
                      <GroupName>Core/modbus/transport</GroupName>
                      <Files>
                        <File><FileName>legacy_old_transport.c</FileName><FileType>1</FileType><FilePath>..\Core\Src\generated\modbus\transport\legacy_old_transport.c</FilePath></File>
                      </Files>
                    </Group>
                    <Cads>
                      <VariousControls>
                        <IncludePath>../Core/Inc;../Drivers/CMSIS/Include</IncludePath>
                      </VariousControls>
                    </Cads>
                  </Groups>
                </Target>
              </Targets>
            </Project>
            """.trimIndent().replace("\n", "\r\n")
        uvprojxPath.writeText(original)

        val updated =
            tool.updateUvprojxContent(
                original = original,
                uvprojxFile = uvprojxPath.toFile(),
                sourceFiles = listOf(sourceA.toFile(), sourceB.toFile(), sourceC.toFile()),
                targetName = "test1",
                groupName = "Core/modbus/rtu",
                transport = ModbusTransportKind.RTU,
            )

        assertContains(updated, "<TargetName>bootloader</TargetName>")
        assertContains(updated, "<FileName>legacy_boot.c</FileName>")
        assertContains(updated, "<FileName>device_generated.c</FileName>")
        assertContains(updated, "<FilePath>..\\Core\\Src\\generated\\modbus\\rtu\\device\\device_generated.c</FilePath>")
        assertContains(updated, "<FileName>device_bridge_impl.c</FileName>")
        assertContains(updated, "<FilePath>..\\Core\\Src\\modbus\\rtu\\device\\device_bridge_impl.c</FilePath>")
        assertContains(updated, "<GroupName>Core/modbus/rtu/device</GroupName>")
        assertContains(updated, "<GroupName>Core/modbus/rtu/transport</GroupName>")
        assertContains(updated, "<FilePath>..\\Core\\Src\\generated\\modbus\\rtu\\transport\\modbus_rtu_dispatch.c</FilePath>")
        assertEquals(0, "<FileName>legacy_modbus.c</FileName>".toRegex().findAll(updated).count())
        assertEquals(0, "<FileName>legacy_transport.c</FileName>".toRegex().findAll(updated).count())
        assertEquals(0, "<FileName>legacy_device.c</FileName>".toRegex().findAll(updated).count())
        assertEquals(0, "<FileName>legacy_old_transport.c</FileName>".toRegex().findAll(updated).count())
        assertEquals(1, "<GroupName>Core/modbus/rtu/transport</GroupName>".toRegex().findAll(updated).count())
        assertContains(updated, "<IncludePath>../Core/Inc;../Drivers/CMSIS/Include;../Core/Inc/generated/modbus/rtu</IncludePath>")
    }

    @Test
    fun `inserts the configured group when it does not exist`() {
        val projectDir = Files.createTempDirectory("keil-sync-insert")
        val uvprojxPath = projectDir.resolve("MDK-ARM/test1.uvprojx").apply { parent.createDirectories() }
        val sourceFile = projectDir.resolve("Core/Src/generated/modbus/rtu/transport/modbus_rtu_dispatch.c").apply { parent.createDirectories() }
        sourceFile.writeText("void dispatch(void) {}")
        val original =
            """
            <Project>
              <Targets>
                <Target>
                  <TargetName>test1</TargetName>
                  <Groups>
                    <Group>
                      <GroupName>Core</GroupName>
                      <Files />
                    </Group>
                  </Groups>
                </Target>
              </Targets>
            </Project>
            """.trimIndent()
        uvprojxPath.writeText(original)

        val updated =
            tool.updateUvprojxContent(
                original = original,
                uvprojxFile = uvprojxPath.toFile(),
                sourceFiles = listOf(sourceFile.toFile()),
                targetName = "test1",
                groupName = "Core/modbus/rtu",
                transport = ModbusTransportKind.RTU,
            )

        assertContains(updated, "<GroupName>Core</GroupName>")
        assertContains(updated, "<GroupName>Core/modbus/rtu/transport</GroupName>")
        assertContains(updated, "<FileName>modbus_rtu_dispatch.c</FileName>")
    }
}
