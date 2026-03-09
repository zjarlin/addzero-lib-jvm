package site.addzero.ksp.metadata.jimmer.entity.external.processor.entity2iso

import generator.IsoCodeGenerator
import site.addzero.entity2iso.processor.context.Settings
import site.addzero.ksp.metadata.jimmer.entity.spi.JimmerEntityProcessContext
import site.addzero.ksp.metadata.jimmer.entity.spi.JimmerEntityProcessorIds
import site.addzero.lsi.processor.ProcessorSpi
import java.io.File

class Entity2IsoExternalProcessor : ProcessorSpi<JimmerEntityProcessContext, Unit> {
    override val id: String = JimmerEntityProcessorIds.ENTITY2_ISO
    override lateinit var ctx: JimmerEntityProcessContext

    override fun onFinish() {
        val context = ctx
        val entities = context.entities
        if (entities.isEmpty()) {
            return
        }

        val logger = context.logger
        Settings.fromOptions(context.options)
        val packageName = Settings.isomorphicPkg
        val outputDir = Settings.isomorphicGenDir
        val outputDirFile = File(outputDir).also { it.mkdirs() }

        logger.warn("开始生成同构体类...")
        var generatedCount = 0

        entities
            .asSequence()
            .sortedBy { it.qualifiedName }
            .forEach { entity ->
                val entitySimpleName = entity.name
                if (entitySimpleName.isBlank()) {
                    logger.error("生成同构体失败: 类名为空")
                    return@forEach
                }

                try {
                    val isoCode = IsoCodeGenerator.generateIsoCode(entity, packageName)
                    val fileName = "${entitySimpleName}Iso.kt"
                    val file = File(outputDirFile, fileName)
                    file.writeText(isoCode)
                    generatedCount++
                    logger.info("生成同构体: ${entitySimpleName}Iso")
                } catch (e: Exception) {
                    logger.error("生成同构体失败: $entitySimpleName, 错误: ${e.message}")
                }
            }

        logger.warn("同构体类生成完成，共生成 $generatedCount 个")
    }
}
