import site.addzero.gradle.plugin.ConsumerKspBuildLogicGenerator
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals

class ConsumerKspBuildLogicGeneratorTest {

    @Test
    fun `buildSpec strips shared modbus prefix and default segment for property names`() {
        val spec =
            ConsumerKspBuildLogicGenerator.buildSpec(
                mustMap =
                    linkedMapOf(
                        "addzero.modbus.codegen.mode" to """listOf("server")""",
                        "addzero.modbus.contractPackages" to ",",
                        "addzero.modbus.tcp.default.host" to "127.0.0.1",
                    ),
                scriptPackageName = "site.addzero.ksp",
                scriptName = "modbus-tcp",
                extensionName = "modbusTcp",
                processorProjectPath = ":lib:ksp:metadata:modbus:modbus-ksp",
                processorArtifactId = "modbus-ksp",
                processorArtifactKind = "JVM",
                companionDependencies = emptyList(),
            )

        assertEquals(listOf("codegenModes", "contractPackages", "tcpHost"), spec.properties.map { it.propertyName })
        assertEquals("site.addzero.ksp.modbus-tcp", spec.pluginId)
        assertEquals("site/addzero/ksp/modbus-tcp/gradle-plugin.properties", spec.coordinatesResourcePath)
        assertEquals("ModbusTcpExtension", spec.extensionClassName)
    }

    @Test
    fun `generateExtensionFile emits typed Gradle extension and optional arg guards`() {
        val spec =
            ConsumerKspBuildLogicGenerator.buildSpec(
                mustMap =
                    linkedMapOf(
                        "springKtor.generatedPackage" to "",
                        "springKtor.enabled" to "true",
                    ),
                scriptPackageName = "site.addzero.ksp",
                scriptName = "spring2ktor-server",
                extensionName = "spring2ktorServer",
                processorProjectPath = ":lib:ksp:metadata:spring2ktor-server-processor",
                processorArtifactId = "spring2ktor-server-processor",
                processorArtifactKind = "JVM",
                companionDependencies = emptyList(),
            )

        val content = ConsumerKspBuildLogicGenerator.generateExtensionFile(spec)
        assertContains(content, "abstract val generatedPackage: Property<String>")
        assertContains(content, "abstract val enabled: Property<Boolean>")
        assertContains(content, "generatedPackage.convention(\"\")")
        assertContains(content, "enabled.convention(true)")
        assertContains(content, "extension.generatedPackage.orNull")
        assertContains(content, "put(\"springKtor.enabled\", extension.enabled.get().toString())")
    }

    @Test
    fun `generateScriptFile keeps direct ksp wiring and companion dependency injection`() {
        val spec =
            ConsumerKspBuildLogicGenerator.buildSpec(
                mustMap =
                    linkedMapOf(
                        "addzero.modbus.codegen.mode" to """listOf("server")""",
                    ),
                scriptPackageName = "site.addzero.ksp",
                scriptName = "modbus-tcp",
                extensionName = "modbusTcp",
                processorProjectPath = ":lib:ksp:metadata:modbus:modbus-ksp",
                processorArtifactId = "modbus-ksp",
                processorArtifactKind = "JVM",
                companionDependencies =
                    listOf("IMPLEMENTATION|KMP|:lib:ksp:metadata:modbus:modbus-runtime|modbus-runtime"),
            )

        val content = ConsumerKspBuildLogicGenerator.generateScriptFile(spec)
        assertContains(content, "pluginManager.apply(\"com.google.devtools.ksp\")")
        assertContains(content, "configurationName = processorConfigurationName(")
        assertContains(content, "localProjectPath = \":lib:ksp:metadata:modbus:modbus-runtime\"")
        assertContains(content, "scope = dependency.scope")
        assertContains(content, "artifactKind = dependency.artifactKind")
        assertContains(content, "collectModbusTcpExtensionKspArgs")
    }
}
