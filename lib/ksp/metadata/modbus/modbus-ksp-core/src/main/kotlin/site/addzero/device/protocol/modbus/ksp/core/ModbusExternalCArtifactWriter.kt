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
 * - `*.h` -> `<project>/Core/Inc/generated/modbus`
 * - generated `*.c` -> `<project>/Core/Src/generated/modbus`
 * - `*_bridge_impl.c` -> 可配置业务实现目录，默认 `<project>/Core/Src/modbus`
 */
class ModbusExternalCArtifactWriter private constructor(
    private val firmwareProjectDir: File,
    private val bridgeImplTargetPath: String,
) {
    private val headerOutputDir: File = firmwareProjectDir.resolve("Core/Inc/generated/modbus")
    private val sourceOutputDir: File = firmwareProjectDir.resolve("Core/Src/generated/modbus")

    fun writeIfSupported(
        artifact: GeneratedArtifact,
        logger: KSPLogger,
    ): File? {
        val targetFile =
            when {
                artifact.extensionName == "h" -> headerOutputDir.resolve("${artifact.fileName}.h")
                artifact.extensionName == "c" && artifact.fileName.endsWith("_bridge_impl") -> resolveBridgeImplFile(artifact)
                artifact.extensionName == "c" -> sourceOutputDir.resolve("${artifact.fileName}.c")
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
            return targetFile
        }
        targetFile.writeText(artifact.content, Charsets.UTF_8)
        if (artifact.fileName.endsWith("_bridge_impl")) {
            cleanupLegacyGeneratedBridgeSource(artifact, logger)
        }
        return targetFile
    }

    private fun resolveBridgeImplFile(artifact: GeneratedArtifact): File {
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

    companion object {
        private const val EXTERNAL_PROJECT_DIR_OPTION = "addzero.modbus.c.output.projectDir"
        private const val EXTERNAL_DIR_OPTION = "addzero.modbus.c.output.dir"
        private const val BRIDGE_IMPL_PATH_OPTION = "addzero.modbus.c.bridgeImpl.path"
        private const val DEFAULT_BRIDGE_IMPL_PATH = "Core/Src/modbus"

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
            environment.logger.logging(
                "Modbus bridge implementation path: ${projectDir.resolve(bridgeImplPath).absolutePath}",
            )
            return ModbusExternalCArtifactWriter(
                firmwareProjectDir = projectDir,
                bridgeImplTargetPath = bridgeImplPath,
            )
        }
    }
}
