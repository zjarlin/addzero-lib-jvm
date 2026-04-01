package site.addzero.device.protocol.modbus.ksp.keil

import java.io.File
import site.addzero.device.protocol.modbus.ksp.core.ModbusProjectSyncContext
import site.addzero.device.protocol.modbus.ksp.core.ModbusProjectSyncTool
import site.addzero.device.protocol.modbus.ksp.core.ModbusTransportKind

/**
 * Keil uVision `.uvprojx` 细粒度同步器。
 *
 * 设计目标：
 * - 只替换一个指定 Group 的 `<Files>` 段
 * - 不重排 Targets / Options / 其他 Groups
 * - 不把整个 XML 交给通用 pretty-printer，避免把 uVision 工程文件改坏
 */
class KeilUvprojxSyncTool : ModbusProjectSyncTool {
    override val toolId: String = "keil-uvprojx"

    override fun isEnabled(context: ModbusProjectSyncContext): Boolean =
        context.environment.options[UVPROJX_PATH_OPTION]?.isNotBlank() == true

    override fun sync(context: ModbusProjectSyncContext) {
        val uvprojxPath = requireNotNull(context.environment.options[UVPROJX_PATH_OPTION]).trim()
        val uvprojxFile = resolveConfiguredFile(context, uvprojxPath)
        if (!uvprojxFile.isFile) {
            context.environment.logger.error("Keil uvprojx file does not exist: ${uvprojxFile.absolutePath}")
            return
        }

        val targetName = context.environment.options[TARGET_NAME_OPTION]?.trim().orEmpty()
        val groupName = context.environment.options[GROUP_NAME_OPTION]?.trim().orEmpty().ifBlank { DEFAULT_GROUP_NAME }
        val sourceFiles = context.externalSourceFiles.filter { file -> file.extension == "c" }.sortedBy { file -> file.name }
        if (sourceFiles.isEmpty()) {
            return
        }

        try {
            val original = uvprojxFile.readText(Charsets.UTF_8)
            val updated =
                updateUvprojxContent(
                    original = original,
                    uvprojxFile = uvprojxFile,
                    sourceFiles = sourceFiles,
                    targetName = targetName,
                    groupName = groupName,
                    transport = context.transport,
                )
            if (updated != original) {
                uvprojxFile.writeText(updated, Charsets.UTF_8)
            }
        } catch (exception: IllegalStateException) {
            context.environment.logger.error(
                "Keil uvprojx sync skipped because the file structure could not be matched: ${exception.message}",
            )
        }
    }

    internal fun updateUvprojxContent(
        original: String,
        uvprojxFile: File,
        sourceFiles: List<File>,
        targetName: String,
        groupName: String,
        transport: ModbusTransportKind,
    ): String {
        val newline = detectNewline(original)
        val targetBlock =
            findBlocks(original, "Target").firstOrNull { block ->
                targetName.isBlank() || block.content.contains("<TargetName>$targetName</TargetName>")
            } ?: error("Cannot find target block in uvprojx: targetName=${targetName.ifBlank { "<first>" }}")
        val updatedTargetBlock =
            updateTargetBlock(
                targetBlock = targetBlock.content,
                uvprojxFile = uvprojxFile,
                sourceFiles = sourceFiles,
                groupName = groupName,
                newline = newline,
                transport = transport,
            )
        return original.replaceRange(targetBlock.range, updatedTargetBlock)
    }

    private fun updateTargetBlock(
        targetBlock: String,
        uvprojxFile: File,
        sourceFiles: List<File>,
        groupName: String,
        newline: String,
        transport: ModbusTransportKind,
    ): String {
        val targetWithUpdatedIncludes =
            updateIncludePaths(
                targetBlock = targetBlock,
                includePathToEnsure = generatedHeaderIncludePath(transport),
            )
        val managedGroups =
            findBlocks(targetWithUpdatedIncludes, "Group").filter { block ->
                val currentGroupName = extractGroupName(block.content)
                currentGroupName == groupName ||
                    currentGroupName.startsWith("$groupName/") ||
                    currentGroupName in legacyGroupNames(groupName, sourceFiles, transport)
            }
        val renderedGroups =
            renderGroups(
                uvprojxFile = uvprojxFile,
                sourceFiles = sourceFiles,
                groupNamePrefix = groupName,
                newline = newline,
                indent = inferGroupIndent(targetWithUpdatedIncludes),
            )
        if (managedGroups.isNotEmpty()) {
            val start = managedGroups.first().range.first
            val endExclusive = managedGroups.last().range.last + 1
            return targetWithUpdatedIncludes.replaceRange(start, endExclusive, renderedGroups)
        }

        val groupsEndRegex = Regex("""</Groups>""")
        val groupsEndMatch = groupsEndRegex.find(targetWithUpdatedIncludes)
            ?: error("Cannot find </Groups> in target block for group insertion")
        val insertion = renderedGroups + newline
        return targetWithUpdatedIncludes.replaceRange(groupsEndMatch.range.first, groupsEndMatch.range.first, insertion)
    }

    private fun updateIncludePaths(
        targetBlock: String,
        includePathToEnsure: String,
    ): String =
        Regex("""<IncludePath>(.*?)</IncludePath>""", setOf(RegexOption.DOT_MATCHES_ALL))
            .replace(targetBlock) { match ->
                val existingValue = match.groupValues[1]
                val updatedValue = ensurePathEntry(existingValue, includePathToEnsure)
                "<IncludePath>$updatedValue</IncludePath>"
            }

    private fun legacyGroupNames(
        groupName: String,
        sourceFiles: List<File>,
        transport: ModbusTransportKind,
    ): Set<String> {
        val suffix = "/${transport.transportId}"
        val legacyPrefix =
            if (groupName.endsWith(suffix)) {
                groupName.removeSuffix(suffix)
            } else {
                return emptySet()
            }
        return sourceFiles
            .map { file -> file.parentFile.name.ifBlank { "misc" } }
            .distinct()
            .mapTo(linkedSetOf()) { groupSuffix -> "$legacyPrefix/$groupSuffix" }
    }

    private fun renderGroups(
        uvprojxFile: File,
        sourceFiles: List<File>,
        groupNamePrefix: String,
        newline: String,
        indent: String,
    ): String =
        buildString {
            val groupedEntries =
                sourceFiles
                    .sortedWith(compareBy<File>({ it.parentFile.name }, { it.name }))
                    .groupBy { file -> file.parentFile.name.ifBlank { "misc" } }
                    .entries
                    .toList()
            groupedEntries.forEachIndexed { index, (groupSuffix, files) ->
                val groupName =
                    if (groupSuffix == "misc") {
                        groupNamePrefix
                    } else {
                        "$groupNamePrefix/$groupSuffix"
                    }
                append(indent).append("<Group>").append(newline)
                append(indent).append("  <GroupName>${escapeXml(groupName)}</GroupName>").append(newline)
                append(indent).append("  <Files>").append(newline)
                files.forEach { file ->
                    append(indent).append("    <File>").append(newline)
                    append(indent).append("      <FileName>${escapeXml(file.name)}</FileName>").append(newline)
                    append(indent).append("      <FileType>1</FileType>").append(newline)
                    append(indent).append("      <FilePath>${escapeXml(toUvprojxPath(uvprojxFile, file))}</FilePath>").append(newline)
                    append(indent).append("    </File>").append(newline)
                }
                append(indent).append("  </Files>").append(newline)
                append(indent).append("</Group>")
                if (index != groupedEntries.lastIndex) {
                    append(newline)
                }
            }
        }

    private fun toUvprojxPath(
        uvprojxFile: File,
        sourceFile: File,
    ): String =
        uvprojxFile.parentFile
            .toPath()
            .relativize(sourceFile.toPath())
            .toString()
            .replace("/", "\\")

    private fun resolveConfiguredFile(
        context: ModbusProjectSyncContext,
        rawPath: String,
    ): File {
        val configured = File(rawPath)
        if (configured.isAbsolute) {
            return configured
        }
        val projectDir =
            context.environment.options["addzero.modbus.c.output.projectDir"]
                ?.let { rawProjectDir -> File(rawProjectDir).absoluteFile }
        return projectDir?.resolve(rawPath)?.absoluteFile ?: configured.absoluteFile
    }

    private fun detectNewline(content: String): String =
        if (content.contains("\r\n")) {
            "\r\n"
        } else {
            "\n"
        }

    private fun findBlocks(
        content: String,
        tagName: String,
    ): List<XmlBlock> {
        val blockRegex = Regex("""(?ms)^([ \t]*)<${tagName}>.*?^[ \t]*</${tagName}>""")
        return blockRegex.findAll(content).map { match ->
            XmlBlock(
                indent = match.groupValues[1],
                range = match.range,
                content = match.value,
            )
        }.toList()
    }

    private fun inferGroupIndent(targetBlock: String): String =
        Regex("""(?m)^([ \t]*)</Groups>""").find(targetBlock)?.groupValues?.get(1)?.plus("  ") ?: "        "

    private fun ensurePathEntry(
        rawValue: String,
        pathEntry: String,
    ): String {
        val entries =
            rawValue
                .split(';')
                .map(String::trim)
                .filter(String::isNotBlank)
                .toMutableList()
        if (entries.none { entry -> entry.equals(pathEntry, ignoreCase = true) }) {
            entries += pathEntry
        }
        return entries.joinToString(";")
    }

    private fun extractGroupName(groupBlock: String): String =
        Regex("""<GroupName>(.*?)</GroupName>""").find(groupBlock)?.groupValues?.get(1).orEmpty()

    private fun generatedHeaderIncludePath(transport: ModbusTransportKind): String =
        "../Core/Inc/generated/modbus/${transport.transportId}"

    private fun escapeXml(value: String): String =
        value
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;")

    private data class XmlBlock(
        val indent: String,
        val range: IntRange,
        val content: String,
    )

    companion object {
        private const val UVPROJX_PATH_OPTION = "addzero.modbus.keil.uvprojx.path"
        private const val TARGET_NAME_OPTION = "addzero.modbus.keil.targetName"
        private const val GROUP_NAME_OPTION = "addzero.modbus.keil.groupName"
        private const val DEFAULT_GROUP_NAME = "Core/modbus"
    }
}
