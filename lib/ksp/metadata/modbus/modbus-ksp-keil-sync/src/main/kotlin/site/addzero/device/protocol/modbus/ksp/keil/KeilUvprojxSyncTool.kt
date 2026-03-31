package site.addzero.device.protocol.modbus.ksp.keil

import java.io.File
import site.addzero.device.protocol.modbus.ksp.core.ModbusProjectSyncContext
import site.addzero.device.protocol.modbus.ksp.core.ModbusProjectSyncTool

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
            val updated = updateUvprojxContent(original, uvprojxFile, sourceFiles, targetName, groupName)
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
    ): String {
        val newline = detectNewline(original)
        val targetBlock =
            findBlocks(original, "Target").firstOrNull { block ->
                targetName.isBlank() || block.content.contains("<TargetName>$targetName</TargetName>")
            } ?: error("Cannot find target block in uvprojx: targetName=${targetName.ifBlank { "<first>" }}")
        val updatedTargetBlock = updateTargetBlock(targetBlock.content, uvprojxFile, sourceFiles, groupName, newline)
        return original.replaceRange(targetBlock.range, updatedTargetBlock)
    }

    private fun updateTargetBlock(
        targetBlock: String,
        uvprojxFile: File,
        sourceFiles: List<File>,
        groupName: String,
        newline: String,
    ): String {
        val groupBlock =
            findBlocks(targetBlock, "Group").firstOrNull { block ->
                block.content.contains("<GroupName>$groupName</GroupName>")
            }
        val renderedGroup =
            renderGroup(
                uvprojxFile = uvprojxFile,
                sourceFiles = sourceFiles,
                groupName = groupName,
                newline = newline,
                indent = inferGroupIndent(targetBlock),
            )
        if (groupBlock != null) {
            return targetBlock.replaceRange(groupBlock.range, renderedGroup)
        }

        val groupsEndRegex = Regex("""</Groups>""")
        val groupsEndMatch = groupsEndRegex.find(targetBlock)
            ?: error("Cannot find </Groups> in target block for group insertion")
        val insertion = renderedGroup + newline
        return targetBlock.replaceRange(groupsEndMatch.range.first, groupsEndMatch.range.first, insertion)
    }

    private fun renderGroup(
        uvprojxFile: File,
        sourceFiles: List<File>,
        groupName: String,
        newline: String,
        indent: String,
    ): String =
        buildString {
            append(indent).append("<Group>").append(newline)
            append(indent).append("  <GroupName>${escapeXml(groupName)}</GroupName>").append(newline)
            append(indent).append("  <Files>").append(newline)
            sourceFiles.forEach { file ->
                append(indent).append("    <File>").append(newline)
                append(indent).append("      <FileName>${escapeXml(file.name)}</FileName>").append(newline)
                append(indent).append("      <FileType>1</FileType>").append(newline)
                append(indent).append("      <FilePath>${escapeXml(toUvprojxPath(uvprojxFile, file))}</FilePath>").append(newline)
                append(indent).append("    </File>").append(newline)
            }
            append(indent).append("  </Files>").append(newline)
            append(indent).append("</Group>")
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
