package site.addzero.device.protocol.modbus.ksp.core

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import java.io.File

/**
 * 可选的外部 C 产物镜像输出。
 *
 * 目的：
 * - Kotlin gateway 仍由 KSP 正常写回当前 Gradle module
 * - C 头文件/源文件额外镜像到固件工程，避免再手动复制
 *
 * 当前落点约定：
 * - `*.h` -> `<project>/Core/Inc/generated/modbus/<transport>/<service-or-transport>`
 * - generated `*.c` -> `<project>/Core/Src/generated/modbus/<transport>/<service-or-transport>`
 * - `*_bridge_impl.c` -> 可配置业务实现根目录下的 `<transport>/<service>` 子目录，默认 `<project>/Core/Src/modbus/<transport>/<service>`
 */
class ModbusExternalCArtifactWriter private constructor(
    private val firmwareProjectDir: File,
    private val bridgeImplTargetPath: String,
    private val markdownOutputPath: String,
) {
    private val headerOutputDir = firmwareProjectDir.resolve("Core/Inc/generated/modbus")
    private val sourceOutputDir = firmwareProjectDir.resolve("Core/Src/generated/modbus")
    private val markdownOutputDir =
        firmwareProjectDir.resolve(markdownOutputPath)

    fun writeIfSupported(
        artifact: GeneratedArtifact,
        logger: KSPLogger,
    ): File? {
        val transportDirName = artifact.externalTransportDirectoryName()
        val groupDirName = artifact.externalGroupDirectoryName()
        val targetFile =
            when {
                artifact.extensionName == "h" -> headerOutputDir.resolve(transportDirName).resolve(groupDirName).resolve("${artifact.fileName}.h")
                artifact.extensionName == "c" && artifact.fileName.endsWith("_bridge_sample") ->
                    markdownOutputDir.resolve(transportDirName).resolve("${artifact.fileName}.c")
                artifact.extensionName == "c" && artifact.fileName.endsWith("_bridge_impl") -> resolveBridgeImplFile(artifact, transportDirName, groupDirName)
                artifact.extensionName == "c" -> sourceOutputDir.resolve(transportDirName).resolve(groupDirName).resolve("${artifact.fileName}.c")
                artifact.extensionName == "md" -> markdownOutputDir.resolve(transportDirName).resolve("${artifact.fileName}.md")
                else -> return null
            }

        val targetDir = targetFile.parentFile
        if (!targetDir.exists() && !targetDir.mkdirs()) {
            logger.error("无法创建 Modbus 外部输出目录：${targetDir.absolutePath}")
            return null
        }

        if (artifact.fileName.endsWith("_bridge_impl") && targetFile.exists()) {
            logger.logging("Skip overwriting editable bridge implementation: ${targetFile.absolutePath}")
            cleanupLegacyGeneratedBridgeSource(artifact, logger)
            cleanupLegacyFlatArtifact(artifact, logger)
            return targetFile
        }
        targetFile.writeText(artifact.content, Charsets.UTF_8)
        if (artifact.fileName.endsWith("_bridge_impl")) {
            cleanupLegacyGeneratedBridgeSource(artifact, logger)
        }
        cleanupLegacyFlatArtifact(artifact, logger)
        cleanupLegacyMarkdownArtifact(artifact, logger)
        cleanupLegacyBridgeReferenceMarkdown(artifact, logger)
        return targetFile
    }

    private fun resolveBridgeImplFile(
        artifact: GeneratedArtifact,
        transportDirName: String,
        groupDirName: String,
    ): File {
        val configuredPath = File(bridgeImplTargetPath)
        val base =
            if (configuredPath.isAbsolute) {
                configuredPath
            } else {
                firmwareProjectDir.resolve(bridgeImplTargetPath)
            }
        val rootDir = if (base.name.endsWith(".c")) base.parentFile else base
        return rootDir.resolve(transportDirName).resolve(groupDirName).resolve("${artifact.fileName}.c")
    }

    private fun cleanupLegacyGeneratedBridgeSource(
        artifact: GeneratedArtifact,
        logger: KSPLogger,
    ) {
        val legacyFileName = artifact.fileName.removeSuffix("_impl")
        val legacyFile = sourceOutputDir.resolve("$legacyFileName.c")
        if (legacyFile.exists() && legacyFile.delete()) {
            logger.logging("Deleted legacy generated bridge source: ${legacyFile.absolutePath}")
        }
    }

    private fun cleanupLegacyFlatArtifact(
        artifact: GeneratedArtifact,
        logger: KSPLogger,
    ) {
        val legacyFile =
            when {
                artifact.extensionName == "h" -> headerOutputDir.resolve("${artifact.fileName}.h")
                artifact.extensionName == "c" && artifact.fileName.endsWith("_bridge_impl") ->
                    resolveLegacyFlatBridgeImplFile(artifact)
                artifact.extensionName == "c" -> sourceOutputDir.resolve("${artifact.fileName}.c")
                else -> null
            } ?: return
        if (legacyFile.exists() && legacyFile.delete()) {
            logger.logging("Deleted legacy flat Modbus artifact: ${legacyFile.absolutePath}")
        }
        val legacyNoTransportFile =
            when {
                artifact.extensionName == "h" ->
                    headerOutputDir.resolve(artifact.externalGroupDirectoryName()).resolve("${artifact.fileName}.h")
                artifact.extensionName == "c" && artifact.fileName.endsWith("_bridge_impl") ->
                    resolveLegacyBridgeImplWithoutTransportDir(artifact)
                artifact.extensionName == "c" ->
                    sourceOutputDir.resolve(artifact.externalGroupDirectoryName()).resolve("${artifact.fileName}.c")
                else -> null
            }
        if (legacyNoTransportFile != null && legacyNoTransportFile.exists() && legacyNoTransportFile.delete()) {
            logger.logging("Deleted legacy non-transport Modbus artifact: ${legacyNoTransportFile.absolutePath}")
        }
    }

    private fun cleanupLegacyMarkdownArtifact(
        artifact: GeneratedArtifact,
        logger: KSPLogger,
    ) {
        if (artifact.extensionName != "md") {
            return
        }
        val legacyMarkdownFile = markdownOutputDir.resolve("protocols").resolve("${artifact.fileName}.md")
        if (legacyMarkdownFile.exists() && legacyMarkdownFile.delete()) {
            logger.logging("Deleted legacy markdown artifact: ${legacyMarkdownFile.absolutePath}")
        }
    }

    private fun cleanupLegacyBridgeReferenceMarkdown(
        artifact: GeneratedArtifact,
        logger: KSPLogger,
    ) {
        if (artifact.extensionName != "c" || !artifact.fileName.endsWith("_bridge_sample")) {
            return
        }
        val transportDirName = artifact.externalTransportDirectoryName()
        val serviceName = artifact.fileName.removeSuffix("_bridge_sample")
        val legacyCandidates =
            listOf(
                markdownOutputDir.resolve(transportDirName).resolve("${serviceName}.${transportDirName}.bridge-reference.md"),
                markdownOutputDir.resolve(transportDirName).resolve("${serviceName.replace('_', '-')}.${transportDirName}.bridge-reference.md"),
            )
        legacyCandidates.forEach { legacyFile ->
            if (legacyFile.exists() && legacyFile.delete()) {
                logger.logging("Deleted legacy bridge reference markdown: ${legacyFile.absolutePath}")
            }
        }
    }

    private fun resolveLegacyFlatBridgeImplFile(artifact: GeneratedArtifact): File {
        val configuredPath = File(bridgeImplTargetPath)
        val base =
            if (configuredPath.isAbsolute) {
                configuredPath
            } else {
                firmwareProjectDir.resolve(bridgeImplTargetPath)
            }
        return if (base.name.endsWith(".c")) {
            base
        } else {
            base.resolve("${artifact.fileName}.c")
        }
    }

    private fun resolveLegacyBridgeImplWithoutTransportDir(artifact: GeneratedArtifact): File {
        val configuredPath = File(bridgeImplTargetPath)
        val base =
            if (configuredPath.isAbsolute) {
                configuredPath
            } else {
                firmwareProjectDir.resolve(bridgeImplTargetPath)
            }
        val rootDir = if (base.name.endsWith(".c")) base.parentFile else base
        return rootDir.resolve(artifact.externalGroupDirectoryName()).resolve("${artifact.fileName}.c")
    }

    private fun GeneratedArtifact.externalTransportDirectoryName(): String =
        when {
            extensionName == "md" -> {
                val fileParts = fileName.split('.')
                fileParts.getOrNull(1)?.takeIf(String::isNotBlank) ?: "common"
            }
            else ->
                packageName
                    ?.removePrefix("generated.modbus.")
                    ?.substringBefore('.')
                    ?.takeIf(String::isNotBlank)
                    ?: "common"
        }

    private fun GeneratedArtifact.externalGroupDirectoryName(): String =
        when {
            fileName.startsWith("modbus_") -> "transport"
            fileName.endsWith("_bridge_sample") -> "sample"
            fileName.endsWith("_generated") -> fileName.removeSuffix("_generated")
            fileName.endsWith("_bridge") -> fileName.removeSuffix("_bridge")
            fileName.endsWith("_bridge_impl") -> fileName.removeSuffix("_bridge_impl")
            else -> "misc"
        }

    companion object {
        private const val EXTERNAL_PROJECT_DIR_OPTION = "addzero.modbus.c.output.projectDir"
        private const val EXTERNAL_DIR_OPTION = "addzero.modbus.c.output.dir"
        private const val BRIDGE_IMPL_PATH_OPTION = "addzero.modbus.c.bridgeImpl.path"
        private const val MARKDOWN_OUTPUT_PATH_OPTION = "addzero.modbus.markdown.output.path"
        private const val DEFAULT_BRIDGE_IMPL_PATH = "Core/Src/modbus"
        private const val DEFAULT_MARKDOWN_OUTPUT_PATH = "Docs/generated/modbus"

        fun from(environment: SymbolProcessorEnvironment): ModbusExternalCArtifactWriter? {
            val rawDir =
                listOfNotNull(
                    environment.options[EXTERNAL_PROJECT_DIR_OPTION],
                    environment.options[EXTERNAL_DIR_OPTION],
                ).firstOrNull { option -> option.isNotBlank() } ?: return null

            val projectDir = File(rawDir).absoluteFile
            if (!projectDir.exists()) {
                environment.logger.error(
                    "配置了 Modbus 外部 C 输出目录，但路径不存在：${projectDir.absolutePath}",
                )
                return null
            }

            environment.logger.logging(
                "Modbus C 产物会额外镜像到固件工程：${projectDir.absolutePath}",
            )
            val bridgeImplPath = environment.options[BRIDGE_IMPL_PATH_OPTION].orEmpty().ifBlank { DEFAULT_BRIDGE_IMPL_PATH }
            val markdownOutputPath = environment.options[MARKDOWN_OUTPUT_PATH_OPTION].orEmpty().ifBlank { DEFAULT_MARKDOWN_OUTPUT_PATH }
            environment.logger.logging(
                "Modbus bridge implementation path: ${projectDir.resolve(bridgeImplPath).absolutePath}",
            )
            environment.logger.logging(
                "Modbus markdown output path: ${projectDir.resolve(markdownOutputPath).absolutePath}",
            )
            return ModbusExternalCArtifactWriter(
                firmwareProjectDir = projectDir,
                bridgeImplTargetPath = bridgeImplPath,
                markdownOutputPath = markdownOutputPath,
            )
        }
    }
}
