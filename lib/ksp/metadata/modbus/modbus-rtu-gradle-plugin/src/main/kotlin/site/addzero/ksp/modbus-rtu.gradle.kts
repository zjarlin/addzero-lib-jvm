package site.addzero.ksp

import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.create
import site.addzero.gradle.kspconsumer.PublishedCompanionDependency
import site.addzero.gradle.kspconsumer.PublishedDependencyScope
import site.addzero.gradle.kspconsumer.PublishedKspArtifactKind
import site.addzero.gradle.kspconsumer.PublishedKspConsumerDefinition
import site.addzero.gradle.kspconsumer.PublishedProcessorArtifact
import site.addzero.gradle.kspconsumer.configurePublishedKspConsumer

abstract class ModbusRtuExtension {
    abstract val codegenModes: ListProperty<String>
    abstract val contractPackages: ListProperty<String>
    abstract val transports: ListProperty<String>
    abstract val cOutputProjectDir: Property<String>
    abstract val bridgeImplPath: Property<String>
    abstract val markdownOutputPath: Property<String>
    abstract val keilUvprojxPath: Property<String>
    abstract val keilTargetName: Property<String>
    abstract val keilGroupName: Property<String>
    abstract val mxprojectPath: Property<String>
    abstract val springRouteOutputDir: Property<String>
    abstract val rtuPortPath: Property<String>
    abstract val rtuUnitId: Property<Int>
    abstract val rtuBaudRate: Property<Int>
    abstract val rtuDataBits: Property<Int>
    abstract val rtuStopBits: Property<Int>
    abstract val rtuParity: Property<String>
    abstract val rtuTimeoutMs: Property<Long>
    abstract val rtuRetries: Property<Int>
    abstract val tcpHost: Property<String>
    abstract val tcpPort: Property<Int>
    abstract val tcpUnitId: Property<Int>
    abstract val tcpTimeoutMs: Property<Long>
    abstract val tcpRetries: Property<Int>

    init {
        codegenModes.convention(listOf("server"))
        contractPackages.convention(emptyList())
        transports.convention(listOf("rtu"))
        cOutputProjectDir.convention("")
        bridgeImplPath.convention("")
        markdownOutputPath.convention("")
        keilUvprojxPath.convention("")
        keilTargetName.convention("")
        keilGroupName.convention("Core/modbus/rtu")
        mxprojectPath.convention("")
        springRouteOutputDir.convention("")
        rtuPortPath.convention("/dev/ttyUSB0")
        rtuUnitId.convention(1)
        rtuBaudRate.convention(9600)
        rtuDataBits.convention(8)
        rtuStopBits.convention(1)
        rtuParity.convention("none")
        rtuTimeoutMs.convention(1_000L)
        rtuRetries.convention(2)
        tcpHost.convention("127.0.0.1")
        tcpPort.convention(502)
        tcpUnitId.convention(1)
        tcpTimeoutMs.convention(1_000L)
        tcpRetries.convention(2)
    }
}

val publishedKspResourceClassLoader = Thread.currentThread().contextClassLoader ?: javaClass.classLoader
val modbusRtuExtension = extensions.create<ModbusRtuExtension>("modbusRtu")

configurePublishedKspConsumer(
    definition = PublishedKspConsumerDefinition(
        pluginId = "site.addzero.ksp.modbus-rtu",
        coordinatesResourcePath = "site/addzero/ksp/modbus-rtu/gradle-plugin.properties",
        resourceClassLoader = publishedKspResourceClassLoader,
        processorArtifact = PublishedProcessorArtifact(
            artifactKind = PublishedKspArtifactKind.JVM,
            localProjectPath = ":lib:ksp:metadata:modbus:modbus-ksp-rtu",
            artifactId = "modbus-ksp-rtu",
        ),
        companionDependencies = listOf(
            PublishedCompanionDependency(
                scope = PublishedDependencyScope.IMPLEMENTATION,
                artifactKind = PublishedKspArtifactKind.KMP,
                localProjectPath = ":lib:ksp:metadata:modbus:modbus-runtime",
                artifactId = "modbus-runtime",
            ),
        ),
    ),
) {
    linkedMapOf<String, String>().apply {
        put("addzero.modbus.codegen.mode", modbusRtuExtension.codegenModes.get().joinToString(","))
        put("addzero.modbus.transports", modbusRtuExtension.transports.get().joinToString(","))
        modbusRtuExtension.contractPackages.get()
            .takeIf { it.isNotEmpty() }
            ?.let { put("addzero.modbus.contractPackages", it.joinToString(",")) }
        modbusRtuExtension.cOutputProjectDir.orNull?.takeIf(String::isNotBlank)
            ?.let { put("addzero.modbus.c.output.projectDir", it) }
        modbusRtuExtension.bridgeImplPath.orNull?.takeIf(String::isNotBlank)
            ?.let { put("addzero.modbus.c.bridgeImpl.path", it) }
        modbusRtuExtension.markdownOutputPath.orNull?.takeIf(String::isNotBlank)
            ?.let { put("addzero.modbus.markdown.output.path", it) }
        modbusRtuExtension.keilUvprojxPath.orNull?.takeIf(String::isNotBlank)
            ?.let { put("addzero.modbus.keil.uvprojx.path", it) }
        modbusRtuExtension.keilTargetName.orNull?.takeIf(String::isNotBlank)
            ?.let { put("addzero.modbus.keil.targetName", it) }
        modbusRtuExtension.keilGroupName.orNull?.takeIf(String::isNotBlank)
            ?.let { put("addzero.modbus.keil.groupName", it) }
        modbusRtuExtension.mxprojectPath.orNull?.takeIf(String::isNotBlank)
            ?.let { put("addzero.modbus.mxproject.path", it) }
        modbusRtuExtension.springRouteOutputDir.orNull?.takeIf(String::isNotBlank)
            ?.let { put("addzero.modbus.spring.route.outputDir", it) }
        put("addzero.modbus.rtu.default.portPath", modbusRtuExtension.rtuPortPath.get())
        put("addzero.modbus.rtu.default.unitId", modbusRtuExtension.rtuUnitId.get().toString())
        put("addzero.modbus.rtu.default.baudRate", modbusRtuExtension.rtuBaudRate.get().toString())
        put("addzero.modbus.rtu.default.dataBits", modbusRtuExtension.rtuDataBits.get().toString())
        put("addzero.modbus.rtu.default.stopBits", modbusRtuExtension.rtuStopBits.get().toString())
        put("addzero.modbus.rtu.default.parity", modbusRtuExtension.rtuParity.get())
        put("addzero.modbus.rtu.default.timeoutMs", modbusRtuExtension.rtuTimeoutMs.get().toString())
        put("addzero.modbus.rtu.default.retries", modbusRtuExtension.rtuRetries.get().toString())
        put("addzero.modbus.tcp.default.host", modbusRtuExtension.tcpHost.get())
        put("addzero.modbus.tcp.default.port", modbusRtuExtension.tcpPort.get().toString())
        put("addzero.modbus.tcp.default.unitId", modbusRtuExtension.tcpUnitId.get().toString())
        put("addzero.modbus.tcp.default.timeoutMs", modbusRtuExtension.tcpTimeoutMs.get().toString())
        put("addzero.modbus.tcp.default.retries", modbusRtuExtension.tcpRetries.get().toString())
    }
}
