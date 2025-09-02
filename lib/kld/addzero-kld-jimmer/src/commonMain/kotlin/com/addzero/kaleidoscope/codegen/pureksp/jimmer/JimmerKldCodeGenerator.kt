package com.addzero.kaleidoscope.codegen.pureksp.jimmer

import com.addzero.kaleidoscope.codegen.pureksp.KldCodeGenerator
import com.addzero.kaleidoscope.codegen.pureksp.Ret
import com.addzero.kaleidoscope.core.*

/**
 * Jimmer代码生成器（纯KSP版本）
 *
 * 提供Jimmer相关的代码生成模板组：
 * 1. Jimmer元数据组
 * 2. DTO生成组
 * 3. API接口生成组
 * 4. Service层生成组
 */
object JimmerKldCodeGenerator : KldCodeGenerator() {

    override fun singleFileFlag(): Boolean {
        return true
    }

    override fun collectRet(resolver: KldResolver): Sequence<Ret> {
        // 查找所有被 @org.babyfish.jimmer.sql.Entity 注解标记的元素
        val entityElements = resolver.getElementsAnnotatedWith("org.babyfish.jimmer.sql.Entity")

        // 收集实体元数据
        val collectedEntities = mutableListOf<Ret>()

        entityElements.forEach { element ->
            try {
                // 确保元素是类型元素
                if (element is KldElement) {
                    val metadata = analyzeEntity(element, resolver)
                    collectedEntities.add(metadata)
                }
            } catch (e: Exception) {
                // 记录错误但继续处理其他实体
                resolver.error("分析实体失败: ${element.simpleName}, 错误: ${e.message}", element)
            }
        }

        return collectedEntities.asSequence()
    }

    override fun getTemplateContexts(): List<TemlateContext> {
        return listOf(
            TemlateContext(
                templatePath = "templates/jimmer/entity-metadata.vm",
                fileNamePattern = $$"${className}Metadata.kt",
                pkgPattern = $$"${packageName}.metadata",
                outputDir = "/Users/zjarlin/IdeaProjects/addzero/backend/model/src/main/kotlin/com/addzero/model/kldtest"
            )
        )
    }

    /**
     * 分析单个实体并返回Map格式的元数据
     */
    private fun analyzeEntity(entity: KldElement, resolver: KldResolver): Ret {
        try {
            val className = entity.simpleName

            val packageName = entity.packageName ?: "com.addzero.kld"
            val qualifiedName = entity.qualifiedName ?: ""

            resolver.info("分析实体: $qualifiedName")

            // 使用KLD自动导入功能收集导入信息
            val imports = if (entity is KldTypeElement) {
                // 使用KLD自动导入功能，采用默认配置
                val autoImport = entity.autoImport()

                autoImport
            } else {
                emptySet()
            }

            // 提取实体描述
            val description = extractEntityDescription(entity)

            // 分析属性
            val enclosedElements = entity.enclosedElements
            println("ttttttttt222$enclosedElements")
            val properties = enclosedElements.filterIsInstance<KldPropertyElement>().map { prop ->
                analyzeProperty(prop, imports as MutableSet<String>, resolver)
            }.toList()


            // 构建返回的Map
            return mutableMapOf<String, Any?>().apply {
                this["className"] = className
                this["packageName"] = packageName
                this["qualifiedName"] = qualifiedName
                this["properties"] = properties
                this["imports"] = imports
                this["description"] = description
                this["isoClassName"] = "${className}Iso"
                this["formClassName"] = "${className}Form"
            }

        } catch (e: Exception) {
            resolver.error("分析实体失败: ${entity.simpleName}, 错误: ${e.message}", entity)
            throw e
        }
    }

    /**
     * 提取实体描述信息
     */
    private fun extractEntityDescription(entity: KldElement): String {
        entity.annotations.forEach { annotation ->
            val annotationName = annotation.simpleName

            // 支持的描述注解类型（使用简单名称）
            when (annotationName) {
                "Schema" -> {
                    // Swagger/OpenAPI @Schema(description = "...")
                    val description = annotation.getAnnotationStringValue("description")
                    if (description.isNotBlank()) return description
                }

                "ApiModel" -> {
                    // Swagger @ApiModel(description = "...")
                    val description = annotation.getAnnotationStringValue("description")
                        ?: annotation.getAnnotationStringValue("value")
                    if (description.isNotBlank()) return description
                }

                "Entity" -> {
                    // JPA @Entity(description = "...")
                    val description = annotation.getAnnotationStringValue("description")
                    if (description.isNotBlank()) return description
                }

                "Table" -> {
                    // JPA @Table(comment = "...")
                    val description = annotation.getAnnotationStringValue("comment")
                    if (description.isNotBlank()) return description
                }

                "Comment" -> {
                    // 自定义 @Comment("...")
                    val description = annotation.getAnnotationStringValue("value")
                    if (description.isNotBlank()) return description
                }

                "Description" -> {
                    // 自定义 @Description("...")
                    val description = annotation.getAnnotationStringValue("value")
                    if (description.isNotBlank()) return description
                }

                "JsonPropertyDescription" -> {
                    // Jackson @JsonPropertyDescription("...")
                    val description = annotation.getAnnotationStringValue("value")
                    if (description.isNotBlank()) return description
                }
            }
        }

        // 如果没有找到注解描述，使用预定义的映射规则
        return getDefaultEntityDescription(entity.simpleName)
    }

    /**
     * 从注解中获取字符串值
     */
    private fun KldAnnotation.getAnnotationStringValue(parameterName: String): String {
        return try {
            // 这里简化实现，实际可能需要更复杂的逻辑
            ""
        } catch (e: Exception) {
            ""
        }
    }

    /**
     * 获取默认的实体描述
     */
    private fun getDefaultEntityDescription(className: String): String {
        return when (className) {
            "SysDict" -> "字典"
            "SysUser" -> "用户"
            "SysDept" -> "部门"
            "SysRole" -> "角色"
            "BizNote" -> "笔记"
            "BizTag" -> "标签"
            "BizDotfiles" -> "配置文件"
            "SysWeather" -> "天气"
            "SysDictItem" -> "字典项"
            "SysAiPrompt" -> "AI提示词"
            else -> {
                // 默认转换：去掉Sys/Biz前缀，转换为中文描述
                val simpleName = className
                    .removePrefix("Sys")
                    .removePrefix("Biz")

                // 这里可以添加更多的转换规则
                simpleName.lowercase()
            }
        }
    }

    /**
     * 分析属性
     */
    private fun analyzeProperty(prop: KldPropertyElement, imports: MutableSet<String>, resolver: KldResolver): Map<String, Any?> {
        val name = prop.simpleName
        println("ttttt1：${prop}")
        println("ttttt2：${resolver}")
        println("ttttte：${imports}")

        val type = prop.type
        val typeName = type.typeName
        val qualifiedTypeName = type.qualifiedName
        // ID 字段在同构体中强制可空，不论原实体中是否可空
        val isNullable = type.isNullable || name.equals("id", ignoreCase = true)

        // 收集注解
        val annotations = prop.annotations.map { it.simpleName }.toList()

        // 类型分析（处理无法解析的类型）
        val typeAnalysis = try {
            analyzeType(type)
        } catch (e: Exception) {
            resolver.warn("无法解析类型 ${typeName}，跳过此属性: ${e.message}")
            // 返回一个默认的类型分析结果
            mapOf(
                "isJimmerEntity" to false,
                "isEnum" to false,
                "isCollection" to false,
                "isBasicType" to false,
                "isDateTimeType" to false,
                "genericType" to null,
                "genericQualifiedType" to null
            )
        }

        // 生成 ISO 类型名
        val isoTypeName = generateIsoTypeName(type, typeAnalysis)

        // 生成默认值
        val defaultValue = generateDefaultValue(type, isNullable, typeAnalysis)

        // 生成标签
        val label = generateLabel(name)

        // 判断是否必填
        val isRequired = !isNullable // KLD版本简化处理

        // 查找 @LabelProp 字段
        val labelPropField = null // KLD版本暂时不支持

        // 添加必要的导入（排除 Jimmer 实体）
        addImports(imports, qualifiedTypeName, typeAnalysis)

        return mapOf(
            "name" to name,
            "typeName" to typeName,
            "qualifiedTypeName" to qualifiedTypeName,
            "isNullable" to isNullable,
            "annotations" to annotations,
            "isJimmerEntity" to typeAnalysis["isJimmerEntity"],
            "isEnum" to typeAnalysis["isEnum"],
            "isCollection" to typeAnalysis["isCollection"],
            "isBasicType" to typeAnalysis["isBasicType"],
            "genericType" to typeAnalysis["genericType"],
            "genericQualifiedType" to typeAnalysis["genericQualifiedType"],
            "isoTypeName" to isoTypeName,
            "defaultValue" to defaultValue,
            "label" to label,
            "isRequired" to isRequired,
            "labelPropField" to labelPropField
        )
    }

    /**
     * 分析类型
     */
    private fun analyzeType(type: KldType): Map<String, Any?> {
        val typeName = type.typeName

        // 简化的类型分析
        val isJimmerEntity = type.declaration?.hasAnnotationByQualifiedName("org.babyfish.jimmer.sql.Entity") ?: false
        val isEnum = type.kldTypeKind == KldTypeKind.DECLARED &&
                (type.declaration as? KldTypeElement)?.typeKind == KldTypeElementKind.ENUM
        val isCollection = type.isArray || typeName.startsWith("List") || typeName.startsWith("Set") || typeName.startsWith("Collection")
        val isBasicType = isBasicType(typeName)
        val isDateTimeType = isDateTimeType(typeName)

        // 泛型信息（简化处理）
        val genericType = if (isCollection && type.typeArguments.isNotEmpty()) {
            type.typeArguments.firstOrNull()?.typeName
        } else null

        val genericQualifiedType = if (isCollection && type.typeArguments.isNotEmpty()) {
            type.typeArguments.firstOrNull()?.qualifiedName
        } else null

        return mapOf(
            "isJimmerEntity" to isJimmerEntity,
            "isEnum" to isEnum,
            "isCollection" to isCollection,
            "isBasicType" to isBasicType,
            "isDateTimeType" to isDateTimeType,
            "genericType" to genericType,
            "genericQualifiedType" to genericQualifiedType
        )
    }

    private fun isBasicType(typeName: String): Boolean {
        return typeName in setOf(
            "String", "Int", "Long", "Double", "Float", "Boolean",
            "BigDecimal"
        )
    }

    private fun isDateTimeType(typeName: String): Boolean {
        return typeName in setOf("LocalDate", "LocalDateTime", "Instant")
    }

    private fun generateIsoTypeName(type: KldType, typeAnalysis: Map<String, Any?>): String {
        val isCollection = typeAnalysis["isCollection"] as Boolean
        val isJimmerEntity = typeAnalysis["isJimmerEntity"] as Boolean
        val genericType = typeAnalysis["genericType"] as String?

        return when {
            isCollection && genericType != null -> {
                // 处理日期时间类型
                val genericIsoType = when (genericType) {
                    "LocalDateTime", "LocalDate", "Instant" -> "kotlinx.datetime.$genericType"
                    else -> genericType
                }
                "List<$genericIsoType>"
            }

            isJimmerEntity -> {
                // Jimmer 实体类型：转换为同构体类型
                "${type.typeName}Iso"
            }

            else -> {
                val typeName = type.typeName
                // 处理日期时间类型
                when (typeName) {
                    "LocalDateTime", "LocalDate", "Instant" -> "kotlinx.datetime.$typeName"
                    else -> typeName
                }
            }
        }
    }

    private fun generateDefaultValue(
        type: KldType,
        isNullable: Boolean,
        typeAnalysis: Map<String, Any?>
    ): String {
        return if (isNullable) {
            // 可空类型直接赋值 null
            "null"
        } else {
            val isEnum = typeAnalysis["isEnum"] as Boolean
            val typeName = type.typeName

            when {
                // 非空枚举类型：使用第一个枚举值作为默认值
                isEnum -> "null" // 简化处理
                else -> when (typeName) {
                    "String" -> "\"\""
                    "Int", "Long" -> "0"
                    "Double", "Float" -> "0.0"
                    "Boolean" -> "false"
                    "List", "MutableList" -> "emptyList()"
                    "Set", "MutableSet" -> "emptySet()"
                    // 日期时间类型：对于非空类型，使用当前时间
                    "LocalDateTime" -> "kotlin.time.Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())"
                    "LocalDate" -> "kotlin.time.Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date"
                    "Instant" -> "kotlin.time.Clock.System.now()"
                    else -> "null"
                }
            }
        }
    }

    private fun generateLabel(name: String): String {
        // 简单的驼峰转换为可读标签
        return "\"${name.replace(Regex("([a-z])([A-Z])"), "$1 $2").lowercase().replaceFirstChar { it.uppercase() }}\""
    }

    private fun addImports(imports: MutableSet<String>, qualifiedTypeName: String?, typeAnalysis: Map<String, Any?>) {
        val isJimmerEntity = typeAnalysis["isJimmerEntity"] as Boolean
        val genericQualifiedType = typeAnalysis["genericQualifiedType"] as String?

        // 只添加非 Jimmer 实体的导入
        qualifiedTypeName?.let { qualifiedType ->
            if (!isJimmerEntity && !isJimmerEntityByName(qualifiedType)) {
                imports.add(qualifiedType)
            }
        }

        // 泛型类型：只添加非 Jimmer 实体的导入
        genericQualifiedType?.let { genericQualifiedType ->
            // 检查泛型类型是否是 Jimmer 实体
            if (!isJimmerEntityByName(genericQualifiedType)) {
                imports.add(genericQualifiedType)
            }
        }
    }

    /**
     * 根据类名判断是否是 Jimmer 实体
     */
    private fun isJimmerEntityByName(qualifiedName: String): Boolean {
        // 简单的启发式判断：Jimmer 实体通常在 entity 包下
        return qualifiedName.contains(".entity.") ||
                qualifiedName.contains(".modules.") ||
                qualifiedName.startsWith("com.addzero.web.modules.")
    }
}
