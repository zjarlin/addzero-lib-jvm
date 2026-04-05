package site.addzero.device.protocol.modbus.ksp.keil

import java.io.File
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
        resolveMxprojectFile(context).isFile

    override fun sync(context: ModbusProjectSyncContext) {
        val mxprojectFile = resolveMxprojectFile(context)
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

    private fun resolveMxprojectFile(context: ModbusProjectSyncContext): File {
        val configured = context.environment.options[MXPROJECT_PATH_OPTION]?.trim().orEmpty()
        if (configured.isNotBlank()) {
            return resolveConfiguredFile(context, configured)
        }
        val projectDir = resolveProjectDir(context) ?: return File(".mxproject")
        return projectDir.resolve(".mxproject")
    }

    private fun resolvePathBaseDir(context: ModbusProjectSyncContext): File {
        val uvprojxPath = context.environment.options[UVPROJX_PATH_OPTION]?.trim().orEmpty()
        if (uvprojxPath.isNotBlank()) {
            return resolveConfiguredFile(context, uvprojxPath).parentFile
        }
        return resolveProjectDir(context)?.resolve("MDK-ARM") ?: File(".")
    }

    private fun resolveConfiguredFile(
        context: ModbusProjectSyncContext,
        rawPath: String,
    ): File {
        val configured = File(rawPath)
        if (configured.isAbsolute) {
            return configured
        }
        val projectDir = resolveProjectDir(context)
        return projectDir?.resolve(rawPath)?.absoluteFile ?: configured.absoluteFile
    }

    private fun resolveProjectDir(context: ModbusProjectSyncContext): File? =
        context.environment.options[PROJECT_DIR_OPTION]
            ?.takeIf(String::isNotBlank)
            ?.let { rawProjectDir -> File(rawProjectDir).absoluteFile }

    private fun toMxprojectPath(
        baseDir: File,
        file: File,
    ): String =
        baseDir
            .toPath()
            .relativize(file.toPath())
            .toString()
            .replace("/", "\\")

    companion object {
        private const val PROJECT_DIR_OPTION = "addzero.modbus.c.output.projectDir"
        private const val UVPROJX_PATH_OPTION = "addzero.modbus.keil.uvprojx.path"
        private const val MXPROJECT_PATH_OPTION = "addzero.modbus.mxproject.path"
    }
}
