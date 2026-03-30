package site.addzero.ksp.metadata.jimmer.entity.external.processor.entity2iso

import generator.IsoCodeGenerator
import site.addzero.entity2iso.processor.context.Settings
import site.addzero.ksp.metadata.jimmer.entity.spi.JimmerEntityProcessorOptions
import site.addzero.ksp.metadata.jimmer.entity.spi.JimmerEntityProcessContext
import site.addzero.ksp.metadata.jimmer.entity.spi.JimmerEntityProcessorIds
import site.addzero.ksp.metadata.jimmer.entity.spi.JimmerGeneratedSourceWriter
import site.addzero.lsi.processor.ProcessorSpi

/**
 * `entity2iso` 子处理器。
 *
 * 在 umbrella 主处理器完成实体元数据收集后，统一生成 `Iso` 数据类。
 */
class Entity2IsoExternalProcessor : ProcessorSpi<JimmerEntityProcessContext, Unit> {
    override val id: String = JimmerEntityProcessorIds.ENTITY2_ISO
    override lateinit var ctx: JimmerEntityProcessContext

    /** 按当前上下文配置输出所有实体对应的 `Iso` 文件。 */
    override fun onFinish() {
        val context = ctx
        val entities = context.entities
        if (entities.isEmpty()) {
            return
        }

        val logger = context.logger
        Settings.fromOptions(context.options)
        val packageName = context.options[JimmerEntityProcessorOptions.ISO_PACKAGE_NAME]
            ?.takeIf(String::isNotBlank)
            ?: context.options[JimmerEntityProcessorOptions.ISO_PACKAGE_NAME_LEGACY]
            ?.takeIf(String::isNotBlank)
            ?: Settings.isomorphicPkg
        val outputDir = context.options[JimmerEntityProcessorOptions.ISO_OUTPUT_DIR]
            ?.takeIf(String::isNotBlank)
            ?: Settings.isomorphicGenDir
        val classSuffix = context.options[JimmerEntityProcessorOptions.ISO_CLASS_SUFFIX]
            ?.takeIf(String::isNotBlank)
            ?: "Iso"
        val serializableEnabled = context.options[JimmerEntityProcessorOptions.ISO_SERIALIZABLE_ENABLED]
            ?.let { value -> value.equals("true", ignoreCase = true) }
            ?: Settings.isomorphicSerializableEnabled

        logger.warn("开始生成同构体类...")
        var generatedCount = 0

        entities
            .asSequence()
            .sortedBy { it.qualifiedName }
            .forEach { entity ->
                val entitySimpleName = entity.simpleName
                if (entitySimpleName.isBlank()) {
                    logger.error("生成同构体失败: 类名为空")
                    return@forEach
                }

                try {
                    val isoCode = IsoCodeGenerator.generateIsoCode(
                        entity = entity,
                        packageName = packageName,
                        classSuffix = classSuffix,
                        serializableEnabled = serializableEnabled,
                    )
                    val fileName = "$entitySimpleName$classSuffix.kt"
                    val file = JimmerGeneratedSourceWriter.writeKotlinFile(
                        rootOutputDir = outputDir,
                        packageName = packageName,
                        fileName = fileName,
                        content = isoCode
                    )
                    generatedCount++
                    logger.info("生成同构体: ${file.absolutePath}")
                } catch (e: Exception) {
                    logger.error("生成同构体失败: $entitySimpleName, 错误: ${e.message}")
                }
            }

        logger.warn("同构体类生成完成，共生成 $generatedCount 个")
    }
}
