package site.addzero.ksp.modbusrtu.gradle

import org.gradle.api.Project
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import site.addzero.gradle.kspconsumer.AbstractPublishedKspConsumerPlugin
import site.addzero.gradle.kspconsumer.PublishedCompanionDependency
import site.addzero.gradle.kspconsumer.PublishedDependencyScope
import site.addzero.gradle.kspconsumer.PublishedKspArtifactKind
import site.addzero.gradle.kspconsumer.PublishedProcessorArtifact

abstract class ModbusRtuExtension {
    /** 要启用的代码生成模式，例如 `server`、`gateway`、`contract`。 */
    abstract val codegenModes: ListProperty<String>

    /** 要扫描的 Modbus 契约包列表。 */
    abstract val contractPackages: ListProperty<String>

    /** 要启用的 transport 列表；支持逗号语义的多 transport 输出，例如 `rtu,tcp`。 */
    abstract val transports: ListProperty<String>

    /** 固件工程根目录；配置后会把生成的 C 头/源文件额外镜像到该工程。 */
    abstract val cOutputProjectDir: Property<String>

    /**
     * 可编辑 bridge 实现根目录。
     *
     * 例如配置为 `Core/Src/modbus` 时，RTU 的业务桥接文件会落到
     * `Core/Src/modbus/rtu/<service>/<service>_bridge_impl.c`。
     */
    abstract val bridgeImplPath: Property<String>

    /** 外部工程 Markdown 协议文档输出目录；相对路径默认 `Docs/generated/modbus`。 */
    abstract val markdownOutputPath: Property<String>

    /** Keil `.uvprojx` 路径；配置后会细粒度同步 generated/modbus 文件组和 include path。 */
    abstract val keilUvprojxPath: Property<String>

    /** Keil target 名称；为空时默认匹配第一个 `<Target>`。 */
    abstract val keilTargetName: Property<String>

    /** Keil group 前缀；RTU 默认是 `Core/modbus/rtu`。 */
    abstract val keilGroupName: Property<String>

    /** STM32CubeMX `.mxproject` 路径；配置后会同步缓存的源文件和头文件路径。 */
    abstract val mxprojectPath: Property<String>

    /** RTU 默认串口路径。 */
    abstract val rtuPortPath: Property<String>

    /** RTU 默认从站地址。 */
    abstract val rtuUnitId: Property<Int>

    /** RTU 默认波特率。 */
    abstract val rtuBaudRate: Property<Int>

    /** RTU 默认数据位。 */
    abstract val rtuDataBits: Property<Int>

    /** RTU 默认停止位。 */
    abstract val rtuStopBits: Property<Int>

    /** RTU 默认校验位。 */
    abstract val rtuParity: Property<String>

    /** RTU 默认超时毫秒。 */
    abstract val rtuTimeoutMs: Property<Long>

    /** RTU 默认重试次数。 */
    abstract val rtuRetries: Property<Int>

    /** TCP 默认主机地址。 */
    abstract val tcpHost: Property<String>

    /** TCP 默认端口。 */
    abstract val tcpPort: Property<Int>

    /** TCP 默认从站地址。 */
    abstract val tcpUnitId: Property<Int>

    /** TCP 默认超时毫秒。 */
    abstract val tcpTimeoutMs: Property<Long>

    /** TCP 默认重试次数。 */
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

class ModbusRtuGradlePlugin : AbstractPublishedKspConsumerPlugin() {
    override val pluginId: String = PLUGIN_ID
    override val coordinatesResourcePath: String = COORDINATES_RESOURCE_PATH
    override val processorArtifact: PublishedProcessorArtifact =
        PublishedProcessorArtifact(
            artifactKind = PublishedKspArtifactKind.JVM,
            localProjectPath = ":lib:ksp:metadata:modbus:modbus-ksp-rtu",
            artifactId = PROCESSOR_ARTIFACT_ID,
        )
    override val companionDependencies: List<PublishedCompanionDependency> =
        listOf(
            PublishedCompanionDependency(
                scope = PublishedDependencyScope.IMPLEMENTATION,
                artifactKind = PublishedKspArtifactKind.JVM,
                localProjectPath = ":lib:ksp:metadata:modbus:modbus-runtime",
                artifactId = "modbus-runtime",
            ),
        )

    override fun createExtension(project: Project): Any {
        return createTypedExtension(project, EXTENSION_NAME, ModbusRtuExtension::class.java)
    }

    override fun collectKspArgs(project: Project, extension: Any?): Map<String, String> {
        val modbus = extension as ModbusRtuExtension
        return linkedMapOf<String, String>().apply {
            put("addzero.modbus.codegen.mode", modbus.codegenModes.get().joinToString(","))
            put("addzero.modbus.transports", modbus.transports.get().joinToString(","))
            modbus.contractPackages.get()
                .takeIf { it.isNotEmpty() }
                ?.let { put("addzero.modbus.contractPackages", it.joinToString(",")) }
            modbus.cOutputProjectDir.orNull?.takeIf(String::isNotBlank)
                ?.let { put("addzero.modbus.c.output.projectDir", it) }
            modbus.bridgeImplPath.orNull?.takeIf(String::isNotBlank)
                ?.let { put("addzero.modbus.c.bridgeImpl.path", it) }
            modbus.markdownOutputPath.orNull?.takeIf(String::isNotBlank)
                ?.let { put("addzero.modbus.markdown.output.path", it) }
            modbus.keilUvprojxPath.orNull?.takeIf(String::isNotBlank)
                ?.let { put("addzero.modbus.keil.uvprojx.path", it) }
            modbus.keilTargetName.orNull?.takeIf(String::isNotBlank)
                ?.let { put("addzero.modbus.keil.targetName", it) }
            modbus.keilGroupName.orNull?.takeIf(String::isNotBlank)
                ?.let { put("addzero.modbus.keil.groupName", it) }
            modbus.mxprojectPath.orNull?.takeIf(String::isNotBlank)
                ?.let { put("addzero.modbus.mxproject.path", it) }
            put("addzero.modbus.rtu.default.portPath", modbus.rtuPortPath.get())
            put("addzero.modbus.rtu.default.unitId", modbus.rtuUnitId.get().toString())
            put("addzero.modbus.rtu.default.baudRate", modbus.rtuBaudRate.get().toString())
            put("addzero.modbus.rtu.default.dataBits", modbus.rtuDataBits.get().toString())
            put("addzero.modbus.rtu.default.stopBits", modbus.rtuStopBits.get().toString())
            put("addzero.modbus.rtu.default.parity", modbus.rtuParity.get())
            put("addzero.modbus.rtu.default.timeoutMs", modbus.rtuTimeoutMs.get().toString())
            put("addzero.modbus.rtu.default.retries", modbus.rtuRetries.get().toString())
            put("addzero.modbus.tcp.default.host", modbus.tcpHost.get())
            put("addzero.modbus.tcp.default.port", modbus.tcpPort.get().toString())
            put("addzero.modbus.tcp.default.unitId", modbus.tcpUnitId.get().toString())
            put("addzero.modbus.tcp.default.timeoutMs", modbus.tcpTimeoutMs.get().toString())
            put("addzero.modbus.tcp.default.retries", modbus.tcpRetries.get().toString())
        }
    }

    companion object {
        const val PLUGIN_ID: String = "site.addzero.ksp.modbus-rtu"
        const val EXTENSION_NAME: String = "modbusRtu"
        const val PROCESSOR_ARTIFACT_ID: String = "modbus-ksp-rtu"
        const val COORDINATES_RESOURCE_PATH: String =
            "site/addzero/ksp/modbus-rtu/gradle-plugin.properties"
    }
}
