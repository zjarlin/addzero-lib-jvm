package site.addzero.ksp.metadata.jimmer.entity.external.processor.entity2form

import site.addzero.context.SettingContext
import site.addzero.generator.FormCodeGenerator
import site.addzero.ksp.metadata.jimmer.entity.spi.JimmerEntityProcessContext
import site.addzero.ksp.metadata.jimmer.entity.spi.JimmerEntityProcessorIds
import site.addzero.lsi.processor.ProcessorSpi

class Entity2FormExternalProcessor : ProcessorSpi<JimmerEntityProcessContext, Unit> {
    override val id: String = JimmerEntityProcessorIds.ENTITY2_FORM
    override val dependsOn: Set<String> = setOf(JimmerEntityProcessorIds.ENTITY2_ISO)
    override lateinit var ctx: JimmerEntityProcessContext

    override fun onFinish() {
        val context = ctx
        val entities = context.entities
        if (entities.isEmpty()) {
            return
        }

        val logger = context.logger
        SettingContext.initialize(context.options)
        val packageName = SettingContext.settings.formPackageName
        val outputDir = SettingContext.settings.formOutputDir
        val generatedFormClasses = mutableSetOf<String>()
        val formCodeGenerator = FormCodeGenerator(logger)

        logger.warn("开始生成表单类...")
        entities
            .asSequence()
            .sortedBy { it.qualifiedName }
            .forEach { entity ->
                val qualifiedName = entity.qualifiedName

                val entitySimpleName = entity.simpleName
                if (entitySimpleName.isBlank()) {
                    logger.error("生成表单失败: 实体类名为空, 实体: $qualifiedName")
                    return@forEach
                }

                if (!generatedFormClasses.add(qualifiedName)) {
                    logger.info("跳过已生成的表单: ${entitySimpleName}Form")
                    return@forEach
                }

                try {
                    formCodeGenerator.writeFormFileWithStrategy(
                        entity = entity,
                        outputDir = outputDir,
                        packageName = packageName
                    )
                    logger.info("生成表单（策略模式）: ${entitySimpleName}Form")
                } catch (e: Exception) {
                    logger.error("生成表单失败: ${entitySimpleName}, 错误: ${e.message}")
                }
            }

        logger.warn("表单类生成完成，共生成 ${generatedFormClasses.size} 个")
    }
}
