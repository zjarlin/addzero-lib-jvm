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

abstract class ModbusMqttExtension {
    abstract val codegenModes: ListProperty<String>
    abstract val contractPackages: ListProperty<String>
    abstract val metadataProviders: ListProperty<String>
    abstract val transports: ListProperty<String>
    abstract val databaseDriverClass: Property<String>
    abstract val databaseJdbcUrl: Property<String>
    abstract val databaseUsername: Property<String>
    abstract val databasePassword: Property<String>
    abstract val databaseQuery: Property<String>
    abstract val databaseJsonColumn: Property<String>
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
    abstract val mqttBrokerUrl: Property<String>
    abstract val mqttClientId: Property<String>
    abstract val mqttRequestTopic: Property<String>
    abstract val mqttResponseTopic: Property<String>
    abstract val mqttQos: Property<Int>
    abstract val mqttTimeoutMs: Property<Long>
    abstract val mqttRetries: Property<Int>

    init {
        codegenModes.convention(listOf("server"))
        contractPackages.convention(emptyList())
        metadataProviders.convention(emptyList())
        transports.convention(listOf("mqtt"))
        databaseDriverClass.convention("")
        databaseJdbcUrl.convention("")
        databaseUsername.convention("")
        databasePassword.convention("")
        databaseQuery.convention("")
        databaseJsonColumn.convention("")
        cOutputProjectDir.convention("")
        bridgeImplPath.convention("")
        markdownOutputPath.convention("")
        keilUvprojxPath.convention("")
        keilTargetName.convention("")
        keilGroupName.convention("Core/modbus/mqtt")
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
        mqttBrokerUrl.convention("tcp://127.0.0.1:1883")
        mqttClientId.convention("modbus-mqtt-client")
        mqttRequestTopic.convention("modbus/request")
        mqttResponseTopic.convention("modbus/response")
        mqttQos.convention(1)
        mqttTimeoutMs.convention(1_000L)
        mqttRetries.convention(2)
    }
}

val publishedKspResourceClassLoader = Thread.currentThread().contextClassLoader ?: javaClass.classLoader
val modbusMqttExtension = extensions.create<ModbusMqttExtension>("modbusMqtt")

configurePublishedKspConsumer(
    definition = PublishedKspConsumerDefinition(
        pluginId = "site.addzero.ksp.modbus-mqtt",
        coordinatesResourcePath = "site/addzero/ksp/modbus-mqtt/gradle-plugin.properties",
        resourceClassLoader = publishedKspResourceClassLoader,
        processorArtifact = PublishedProcessorArtifact(
            artifactKind = PublishedKspArtifactKind.JVM,
            localProjectPath = ":lib:ksp:metadata:modbus:modbus-ksp-mqtt",
            artifactId = "modbus-ksp-mqtt",
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
        put("addzero.modbus.codegen.mode", modbusMqttExtension.codegenModes.get().joinToString(","))
        put("addzero.modbus.transports", modbusMqttExtension.transports.get().joinToString(","))
        modbusMqttExtension.contractPackages.get()
            .takeIf { it.isNotEmpty() }
            ?.let { put("addzero.modbus.contractPackages", it.joinToString(",")) }
        modbusMqttExtension.metadataProviders.get()
            .takeIf { it.isNotEmpty() }
            ?.let { put("addzero.modbus.metadata.providers", it.joinToString(",")) }
        modbusMqttExtension.databaseDriverClass.orNull?.takeIf(String::isNotBlank)
            ?.let { put("addzero.modbus.database.driverClass", it) }
        modbusMqttExtension.databaseJdbcUrl.orNull?.takeIf(String::isNotBlank)
            ?.let { put("addzero.modbus.database.jdbcUrl", it) }
        modbusMqttExtension.databaseUsername.orNull?.takeIf(String::isNotBlank)
            ?.let { put("addzero.modbus.database.username", it) }
        modbusMqttExtension.databasePassword.orNull?.takeIf(String::isNotBlank)
            ?.let { put("addzero.modbus.database.password", it) }
        modbusMqttExtension.databaseQuery.orNull?.takeIf(String::isNotBlank)
            ?.let { put("addzero.modbus.database.query", it) }
        modbusMqttExtension.databaseJsonColumn.orNull?.takeIf(String::isNotBlank)
            ?.let { put("addzero.modbus.database.jsonColumn", it) }
        modbusMqttExtension.cOutputProjectDir.orNull?.takeIf(String::isNotBlank)
            ?.let { put("addzero.modbus.c.output.projectDir", it) }
        modbusMqttExtension.bridgeImplPath.orNull?.takeIf(String::isNotBlank)
            ?.let { put("addzero.modbus.c.bridgeImpl.path", it) }
        modbusMqttExtension.markdownOutputPath.orNull?.takeIf(String::isNotBlank)
            ?.let { put("addzero.modbus.markdown.output.path", it) }
        modbusMqttExtension.keilUvprojxPath.orNull?.takeIf(String::isNotBlank)
            ?.let { put("addzero.modbus.keil.uvprojx.path", it) }
        modbusMqttExtension.keilTargetName.orNull?.takeIf(String::isNotBlank)
            ?.let { put("addzero.modbus.keil.targetName", it) }
        modbusMqttExtension.keilGroupName.orNull?.takeIf(String::isNotBlank)
            ?.let { put("addzero.modbus.keil.groupName", it) }
        modbusMqttExtension.mxprojectPath.orNull?.takeIf(String::isNotBlank)
            ?.let { put("addzero.modbus.mxproject.path", it) }
        modbusMqttExtension.springRouteOutputDir.orNull?.takeIf(String::isNotBlank)
            ?.let { put("addzero.modbus.spring.route.outputDir", it) }
        put("addzero.modbus.rtu.default.portPath", modbusMqttExtension.rtuPortPath.get())
        put("addzero.modbus.rtu.default.unitId", modbusMqttExtension.rtuUnitId.get().toString())
        put("addzero.modbus.rtu.default.baudRate", modbusMqttExtension.rtuBaudRate.get().toString())
        put("addzero.modbus.rtu.default.dataBits", modbusMqttExtension.rtuDataBits.get().toString())
        put("addzero.modbus.rtu.default.stopBits", modbusMqttExtension.rtuStopBits.get().toString())
        put("addzero.modbus.rtu.default.parity", modbusMqttExtension.rtuParity.get())
        put("addzero.modbus.rtu.default.timeoutMs", modbusMqttExtension.rtuTimeoutMs.get().toString())
        put("addzero.modbus.rtu.default.retries", modbusMqttExtension.rtuRetries.get().toString())
        put("addzero.modbus.tcp.default.host", modbusMqttExtension.tcpHost.get())
        put("addzero.modbus.tcp.default.port", modbusMqttExtension.tcpPort.get().toString())
        put("addzero.modbus.tcp.default.unitId", modbusMqttExtension.tcpUnitId.get().toString())
        put("addzero.modbus.tcp.default.timeoutMs", modbusMqttExtension.tcpTimeoutMs.get().toString())
        put("addzero.modbus.tcp.default.retries", modbusMqttExtension.tcpRetries.get().toString())
        put("addzero.modbus.mqtt.default.brokerUrl", modbusMqttExtension.mqttBrokerUrl.get())
        put("addzero.modbus.mqtt.default.clientId", modbusMqttExtension.mqttClientId.get())
        put("addzero.modbus.mqtt.default.requestTopic", modbusMqttExtension.mqttRequestTopic.get())
        put("addzero.modbus.mqtt.default.responseTopic", modbusMqttExtension.mqttResponseTopic.get())
        put("addzero.modbus.mqtt.default.qos", modbusMqttExtension.mqttQos.get().toString())
        put("addzero.modbus.mqtt.default.timeoutMs", modbusMqttExtension.mqttTimeoutMs.get().toString())
        put("addzero.modbus.mqtt.default.retries", modbusMqttExtension.mqttRetries.get().toString())
    }
}
