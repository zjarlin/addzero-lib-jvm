package site.addzero.ksp.metadata.jimmer.entity.external.processor.entity2mcp

import site.addzero.context.SettingContext
import site.addzero.ksp.metadata.jimmer.entity.spi.JimmerEntityMeta
import site.addzero.ksp.metadata.jimmer.entity.spi.JimmerEntityProcessContext
import site.addzero.ksp.metadata.jimmer.entity.spi.JimmerEntityProcessorIds
import site.addzero.ksp.metadata.jimmer.entity.spi.JimmerGeneratedSourceWriter
import site.addzero.lsi.processor.ProcessorSpi

class Entity2McpExternalProcessor : ProcessorSpi<JimmerEntityProcessContext, Unit> {
    override val id: String = JimmerEntityProcessorIds.ENTITY2_MCP
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
        val packageName = SettingContext.settings.mcpPackageName
        val generatedMcpServices = mutableSetOf<String>()
        val mcpCodeGenerator = McpServiceCodeGenerator()

        logger.warn("开始生成MCP服务类...")
        entities
            .asSequence()
            .sortedBy { it.qualifiedName }
            .forEach { entity ->
                val qualifiedName = entity.qualifiedName
                val className = entity.simpleName
                if (className.isBlank()) {
                    logger.error("生成MCP服务失败: 实体类名为空, 实体: $qualifiedName")
                    return@forEach
                }

                if (!generatedMcpServices.add(qualifiedName)) {
                    logger.warn("MCP服务已存在，跳过生成: ${className}McpService")
                    return@forEach
                }

                try {
                    mcpCodeGenerator.generateMcpService(
                        entity = entity,
                        packageName = packageName
                    )
                    logger.warn("成功生成MCP服务: ${className}McpService")
                } catch (e: Exception) {
                    logger.error("生成MCP服务失败: $className, 错误: ${e.message}")
                }
            }

        logger.warn("MCP服务生成完成，共生成 ${generatedMcpServices.size} 个服务")
    }
}

class McpServiceCodeGenerator {
    fun generateMcpService(
        entity: JimmerEntityMeta,
        packageName: String
    ) {
        val entityName = entity.simpleName
        val entityPackageName = entity.packageName
        val entityDescription = entity.docComment
            .lineSequence()
            .firstOrNull()
            ?.trim()
            .orEmpty()
            .ifBlank { entityName.lowercase() }
        generateMcpService(
            packageName = packageName,
            entityName = entityName,
            entityPackageName = entityPackageName,
            entityDescription = entityDescription
        )
    }

    private fun generateMcpService(
        packageName: String,
        entityName: String,
        entityPackageName: String,
        entityDescription: String
    ) {
        val serviceClassName = "${entityName}McpService"
        val entityFullName = if (entityPackageName.isBlank()) {
            entityName
        } else {
            "${entityPackageName}.${entityName}"
        }
        val settings = SettingContext.settings
        val isomorphicPackageName = settings.isomorphicPackageName
        val isomorphicClassSuffix = settings.isomorphicClassSuffix
        val isoFullName = "$isomorphicPackageName.${entityName}$isomorphicClassSuffix"

        val fileContent = generateServiceFileContent(
            packageName = packageName,
            serviceClassName = serviceClassName,
            entityName = entityName,
            entityFullName = entityFullName,
            isoFullName = isoFullName,
            entityDescription = entityDescription
        )
        JimmerGeneratedSourceWriter.writeKotlinFile(
            rootOutputDir = settings.backendServerSourceDir,
            packageName = packageName,
            fileName = "$serviceClassName.kt",
            content = fileContent
        )
    }

    private fun generateServiceFileContent(
        packageName: String,
        serviceClassName: String,
        entityName: String,
        entityFullName: String,
        isoFullName: String,
        entityDescription: String
    ): String {
        return """
            package $packageName

            import site.addzero.common.consts.sql
            import site.addzero.web.infra.jackson.toJson
            import org.babyfish.jimmer.ImmutableObjects
            import org.springframework.ai.tool.annotation.Tool
            import org.springframework.ai.tool.annotation.ToolParam
            import org.springframework.stereotype.Service
            import $entityFullName
            import $isoFullName

            /**
             * $entityName MCP服务
             *
             * 提供${entityDescription}相关的CRUD操作和AI工具
             * 自动生成的代码，请勿手动修改
             */
            @Service
            class $serviceClassName  {

                @Tool(description = "保存${entityDescription}数据到数据库")
                fun save$entityName(@ToolParam(description = "${entityDescription}数据对象") entity: ${entityName}Iso): String {
                       val toJson = entity.toJson()
        val fromString = ImmutableObjects.fromString(${entityName}::class.java, toJson)
        val save = sql.save(fromString)
        return "保存成功"

                }
            }
        """.trimIndent()
    }
}
