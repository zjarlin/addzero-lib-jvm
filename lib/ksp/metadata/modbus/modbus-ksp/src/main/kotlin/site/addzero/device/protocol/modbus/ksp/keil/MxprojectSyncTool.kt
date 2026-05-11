package site.addzero.device.protocol.modbus.ksp.keil

import java.io.File
import site.addzero.device.protocol.modbus.ksp.core.ModbusConsumerCArtifactsConfig
import site.addzero.device.protocol.modbus.ksp.core.ModbusConsumerCArtifactsSupport
import site.addzero.device.protocol.modbus.ksp.core.ModbusProjectSyncContext
import site.addzero.device.protocol.modbus.ksp.core.ModbusProjectSyncTool
import site.addzero.device.protocol.modbus.ksp.core.ModbusTransportKind

/**
 * STM32CubeMX `.mxproject` 路径缓存同步器。
 *
 * 目标：
 * - 只更新路径缓存字段，不碰其他 CubeMX 元数据
 * - 让 CubeMX / MDK 重新打开工程时仍能看到 generated/modbus 与 modbus/<transport> 目录
 */
class MxprojectSyncTool : ModbusProjectSyncTool {
    override val toolId = "stm32cubemx-mxproject"

    override fun isEnabled(context: ModbusProjectSyncContext): Boolean =
        resolveMxprojectFile(context)?.isFile == true

    override fun sync(context: ModbusProjectSyncContext) {
        val mxprojectFile = resolveMxprojectFile(context) ?: return
        if (!mxprojectFile.isFile) {
            return
        }
        val original = mxprojectFile.readText(Charsets.UTF_8)
        val updated =
            updateMxprojectContent(
                original = original,
                transport = context.transport,
                externalSourceFiles = context.externalSourceFiles,
                baseDir = resolvePathBaseDir(context),
            )
        if (updated != original) {
            mxprojectFile.writeText(updated, Charsets.UTF_8)
        }
    }

    internal fun updateMxprojectContent(
        original: String,
        transport: ModbusTransportKind,
        externalSourceFiles: List<File>,
        baseDir: File,
    ): String {
        val sourceEntries =
            externalSourceFiles
                .filter { file -> file.extension == "c" }
                .map { file -> toMxprojectPath(baseDir, file) }
                .distinct()
        val headerEntries =
            linkedSetOf(
                "..\\Core\\Inc",
                "..\\Core\\Inc\\generated\\modbus\\${transport.transportId}",
            )
        val sourceFolderEntries =
            linkedSetOf(
                "..\\Core\\Src",
                "..\\Core\\Src\\generated\\modbus\\${transport.transportId}",
                "..\\Core\\Src\\modbus\\${transport.transportId}",
            )

        var updated = original
        updated =
            updateSectionKey(
                content = updated,
                sectionName = "PreviousUsedKeilFiles",
                key = "SourceFiles",
            ) { value -> mergeSemicolonList(value, sourceEntries) }
        updated =
            updateSectionKey(
                content = updated,
                sectionName = "PreviousUsedKeilFiles",
                key = "HeaderPath",
            ) { value -> mergeSemicolonList(value, headerEntries) }
        updated =
            updateIndexedListSection(
                content = updated,
                sectionName = "PreviousGenFiles",
                listBaseName = "HeaderPath",
                entries = headerEntries.toList(),
            )
        updated =
            updateIndexedListSection(
                content = updated,
                sectionName = "PreviousGenFiles",
                listBaseName = "SourcePath",
                entries = sourceFolderEntries.toList(),
            )
        return updated
    }

    private fun updateIndexedListSection(
        content: String,
        sectionName: String,
        listBaseName: String,
        entries: List<String>,
    ): String {
        var updated = content
        updated =
            updateSectionKey(updated, sectionName, "${listBaseName.removeSuffix("Path")}FolderListSize") {
                entries.size.toString()
            }
        val sectionRegex = Regex("""(?ms)(\[$sectionName]\R)(.*?)(?=\R\[|\z)""")
        val match = sectionRegex.find(updated) ?: return updated
        var sectionBody = match.groupValues[2]
        sectionBody = sectionBody.replace(Regex("""(?m)^$listBaseName#\d+=.*(?:\R|$)"""), "")
        val normalizedBody = if (sectionBody.isNotEmpty() && !sectionBody.endsWith("\n")) "$sectionBody\n" else sectionBody
        val appended =
            buildString {
                append(normalizedBody)
                entries.forEachIndexed { index, entry ->
                    append("$listBaseName#$index=$entry\n")
                }
            }.trimEnd('\n', '\r')
        return updated.replaceRange(match.range, "[${sectionName}]\n$appended")
    }

    private fun updateSectionKey(
        content: String,
        sectionName: String,
        key: String,
        transform: (String) -> String,
    ): String {
        val sectionRegex = Regex("""(?ms)(\[$sectionName]\R)(.*?)(?=\R\[|\z)""")
        val sectionMatch = sectionRegex.find(content) ?: return content
        val sectionContent = sectionMatch.groupValues[2]
        val keyRegex = Regex("""(?m)^($key=)(.*)$""")
        val keyMatch = keyRegex.find(sectionContent) ?: return content
        val replacedLine = keyMatch.groupValues[1] + transform(keyMatch.groupValues[2])
        val updatedSectionContent = sectionContent.replaceRange(keyMatch.range, replacedLine)
        return content.replaceRange(sectionMatch.range, "[${sectionName}]\n${updatedSectionContent.trimEnd('\n', '\r')}")
    }

    private fun mergeSemicolonList(
        rawValue: String,
        entriesToAppend: Iterable<String>,
    ): String {
        val entries =
            rawValue
                .split(';')
                .map(String::trim)
                .filter(String::isNotBlank)
                .toMutableList()
        entriesToAppend.forEach { entry ->
            if (entries.none { current -> current.equals(entry, ignoreCase = true) }) {
                entries += entry
            }
        }
        return entries.joinToString(";")
    }

    private fun resolveMxprojectFile(context: ModbusProjectSyncContext): File? {
        val config = resolveConfig(context) ?: return null
        val configured = config.mxprojectPath?.trim().orEmpty()
        if (configured.isNotBlank()) {
            return resolveConfiguredFile(config, configured)
        }
        return config.firmwareProjectDir.resolve(".mxproject")
    }

    private fun resolvePathBaseDir(context: ModbusProjectSyncContext): File {
        val config = resolveConfig(context)
        val uvprojxPath = config?.keilUvprojxPath?.trim().orEmpty()
        if (uvprojxPath.isNotBlank()) {
            return resolveConfiguredFile(requireNotNull(config), uvprojxPath).parentFile
        }
        return config?.firmwareProjectDir?.resolve("MDK-ARM") ?: File(".")
    }

    private fun resolveConfiguredFile(
        config: ModbusConsumerCArtifactsConfig,
        rawPath: String,
    ): File {
        val configured = File(rawPath)
        if (configured.isAbsolute) {
            return configured
        }
        return config.firmwareProjectDir.resolve(rawPath).absoluteFile
    }

    private fun toMxprojectPath(
        baseDir: File,
        file: File,
    ): String =
        baseDir
            .toPath()
            .relativize(file.toPath())
            .toString()
            .replace("/", "\\")

    private fun resolveConfig(context: ModbusProjectSyncContext): ModbusConsumerCArtifactsConfig? =
        ModbusConsumerCArtifactsSupport.resolve(
            environment = context.environment,
            transport = context.transport,
        )

    companion object {
    }
}
