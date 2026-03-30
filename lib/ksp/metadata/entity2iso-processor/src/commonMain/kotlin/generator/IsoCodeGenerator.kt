package generator

import site.addzero.ksp.metadata.jimmer.entity.spi.JimmerEntityMeta
import site.addzero.ksp.metadata.jimmer.entity.spi.JimmerPropertyMeta
import site.addzero.ksp.metadata.jimmer.entity.spi.JimmerTypeKind
import site.addzero.ksp.metadata.jimmer.entity.spi.JimmerTypeRef

/**
 * `Iso` Kotlin 源码生成器。
 *
 * 负责把 Jimmer 实体元数据转换成可序列化或纯 Kotlin 的 `data class` 文本。
 */
object IsoCodeGenerator {
    /** 根据实体元数据生成完整的 `Iso` Kotlin 源码。 */
    fun generateIsoCode(
        entity: JimmerEntityMeta,
        packageName: String,
        classSuffix: String,
        serializableEnabled: Boolean = true,
    ): String {
        val propertyModels = entity.properties.map { property ->
            val typeResult = buildIsoType(
                type = property.type,
                classSuffix = classSuffix,
                serializableEnabled = serializableEnabled,
            )
            val defaultValueResult = defaultValueFor(property, typeResult)
            val contextualAnnotation = if (serializableEnabled && typeResult.contextual) "@Contextual " else ""
            val nullableSuffix = if (property.type.nullable) "?" else ""
            val propertyDeclaration =
                "    ${contextualAnnotation}val ${property.name}: ${typeResult.rendered}$nullableSuffix = ${defaultValueResult.code}"
            val propertyKDoc = renderKDoc(property.docComment, indent = "    ")

            PropertyModel(
                code = listOfNotNull(propertyKDoc, propertyDeclaration).joinToString("\n"),
                imports = typeResult.imports + defaultValueResult.imports,
                needsExperimentalTimeOptIn = defaultValueResult.needsExperimentalTimeOptIn,
            )
        }

        val props = propertyModels.joinToString(",\n") { it.code }
        val imports = propertyModels.flatMap { it.imports }.toMutableSet()
        if (serializableEnabled) {
            imports.add("import kotlinx.serialization.Serializable")
        }
        val isoClassName = "${entity.simpleName}$classSuffix"

        return buildString {
            appendLine("package $packageName")
            appendLine()
            if (imports.isNotEmpty()) {
                imports.sorted().forEach { appendLine(it) }
                appendLine()
            }
            renderKDoc(entity.docComment)?.let { kdoc ->
                appendLine(kdoc)
            }
            if (serializableEnabled) {
                appendLine("@Serializable")
            }
            appendLine("data class $isoClassName(")
            if (props.isNotBlank()) {
                appendLine(props)
            }
            append(")")
        }.trimEnd()
    }

    private data class PropertyModel(
        val code: String,
        val imports: Set<String>,
        val needsExperimentalTimeOptIn: Boolean,
    )

    private data class IsoTypeResult(
        val rendered: String,
        val imports: Set<String> = emptySet(),
        val kind: JimmerTypeKind = JimmerTypeKind.OTHER,
        val contextual: Boolean = false,
    )

    private data class DefaultValueResult(
        val code: String,
        val imports: Set<String> = emptySet(),
        val needsExperimentalTimeOptIn: Boolean = false,
    )

    /** 把元数据类型映射成 `Iso` 属性类型。 */
    private fun buildIsoType(
        type: JimmerTypeRef,
        classSuffix: String,
        serializableEnabled: Boolean,
    ): IsoTypeResult {
        if (type.kind == JimmerTypeKind.ARRAY) {
            return buildIsoArrayType(type, classSuffix, serializableEnabled)
        }

        if (type.kind == JimmerTypeKind.COLLECTION) {
            val renderedArgs = type.typeArguments.map {
                buildIsoType(
                    type = it,
                    classSuffix = classSuffix,
                    serializableEnabled = serializableEnabled,
                )
            }
            val rendered = if (renderedArgs.isNotEmpty()) {
                "${type.simpleName}<${renderedArgs.joinToString(", ") { it.rendered }}>"
            } else {
                type.simpleName
            }
            val imports = renderedArgs.flatMap { it.imports }.toSet()
            return IsoTypeResult(rendered = rendered, imports = imports, kind = JimmerTypeKind.COLLECTION)
        }

        mapToKotlinPrimitive(type.qualifiedName, type.simpleName)?.let { primitive ->
            return IsoTypeResult(rendered = primitive, kind = JimmerTypeKind.BASIC)
        }

        if (type.qualifiedName == "kotlin.String" || type.qualifiedName == "java.lang.String" || type.simpleName == "String") {
            return IsoTypeResult(rendered = "String", kind = JimmerTypeKind.BASIC)
        }

        if (type.qualifiedName == "java.math.BigDecimal" || type.simpleName == "BigDecimal") {
            return IsoTypeResult(
                rendered = "BigDecimal",
                imports = setOf("import java.math.BigDecimal"),
                kind = JimmerTypeKind.BASIC,
            )
        }

        mapToKotlinxDateTime(type.qualifiedName, type.simpleName)?.let { mapped ->
            val contextual = serializableEnabled && mapped in setOf("LocalDateTime", "LocalDate", "Instant")
            return IsoTypeResult(
                rendered = mapped,
                imports = setOf("import kotlinx.datetime.$mapped") +
                    if (contextual) setOf("import kotlinx.serialization.Contextual") else emptySet(),
                kind = JimmerTypeKind.DATE_TIME,
                contextual = contextual,
            )
        }

        if (type.qualifiedName in setOf("java.util.Date", "java.sql.Timestamp")) {
            return IsoTypeResult(rendered = "Long", kind = JimmerTypeKind.BASIC)
        }

        if (type.kind == JimmerTypeKind.ENUM) {
            val imports = type.qualifiedName
                ?.takeIf { shouldImport(it) }
                ?.let { setOf("import $it") }
                .orEmpty()
            return IsoTypeResult(rendered = type.simpleName, imports = imports, kind = JimmerTypeKind.ENUM)
        }

        if (type.kind == JimmerTypeKind.ENTITY) {
            return IsoTypeResult(rendered = "${type.simpleName}$classSuffix", kind = JimmerTypeKind.ENTITY)
        }

        val imports = type.qualifiedName
            ?.takeIf { shouldImport(it) }
            ?.let { setOf("import $it") }
            .orEmpty()
        return IsoTypeResult(rendered = type.simpleName, imports = imports, kind = JimmerTypeKind.OTHER)
    }

    /** 处理数组类型，递归映射其泛型参数。 */
    private fun buildIsoArrayType(
        type: JimmerTypeRef,
        classSuffix: String,
        serializableEnabled: Boolean,
    ): IsoTypeResult {
        if (type.simpleName != "Array" && (type.qualifiedName?.startsWith("kotlin.") == true)) {
            return IsoTypeResult(rendered = type.simpleName, kind = JimmerTypeKind.ARRAY)
        }

        val arg = type.typeArguments.firstOrNull()?.let {
            buildIsoType(
                type = it,
                classSuffix = classSuffix,
                serializableEnabled = serializableEnabled,
            )
        }
        val rendered = if (arg != null) "Array<${arg.rendered}>" else "Array<Any>"
        val imports = arg?.imports.orEmpty()
        return IsoTypeResult(rendered = rendered, imports = imports, kind = JimmerTypeKind.ARRAY)
    }

    /** 将原始注释文本规范化为 KDoc 块。 */
    private fun renderKDoc(rawDocComment: String, indent: String = ""): String? {
        val lines = normalizeDocCommentLines(rawDocComment)
        if (lines.isEmpty()) {
            return null
        }

        return buildString {
            append(indent)
            appendLine("/**")
            lines.forEach { line ->
                append(indent)
                append(" *")
                if (line.isNotBlank()) {
                    append(" ")
                    append(line)
                }
                appendLine()
            }
            append(indent)
            append(" */")
        }
    }

    /** 清洗注释文本，去掉已有的 KDoc 包装和多余空行。 */
    private fun normalizeDocCommentLines(rawDocComment: String): List<String> {
        val doc = rawDocComment.trim()
        if (doc.isBlank()) {
            return emptyList()
        }

        val normalizedLines = doc
            .removePrefix("/**")
            .removeSuffix("*/")
            .trim()
            .lines()
            .map { line ->
                line.trim()
                    .removePrefix("*")
                    .trim()
            }
            .toMutableList()

        while (normalizedLines.firstOrNull()?.isBlank() == true) {
            normalizedLines.removeAt(0)
        }
        while (normalizedLines.lastOrNull()?.isBlank() == true) {
            normalizedLines.removeAt(normalizedLines.lastIndex)
        }
        return normalizedLines
    }

    /** 为属性生成默认值表达式。 */
    private fun defaultValueFor(property: JimmerPropertyMeta, type: IsoTypeResult): DefaultValueResult {
        if (property.type.nullable) {
            return DefaultValueResult("null")
        }

        return when (type.kind) {
            JimmerTypeKind.BASIC -> when (type.rendered) {
                "String" -> DefaultValueResult("\"\"")
                "Int" -> DefaultValueResult("0")
                "Long" -> DefaultValueResult("0L")
                "Double" -> DefaultValueResult("0.0")
                "Float" -> DefaultValueResult("0f")
                "Boolean" -> DefaultValueResult("false")
                "Byte" -> DefaultValueResult("0")
                "Short" -> DefaultValueResult("0")
                "Char" -> DefaultValueResult("' '")
                "BigDecimal" -> DefaultValueResult("BigDecimal.ZERO")
                else -> DefaultValueResult("TODO()")
            }

            JimmerTypeKind.COLLECTION -> DefaultValueResult(
                code = when {
                    type.rendered.startsWith("MutableList") -> "mutableListOf()"
                    type.rendered.startsWith("MutableSet") -> "mutableSetOf()"
                    type.rendered.startsWith("MutableMap") -> "mutableMapOf()"
                    type.rendered.startsWith("List") -> "emptyList()"
                    type.rendered.startsWith("Set") -> "emptySet()"
                    type.rendered.startsWith("Map") -> "emptyMap()"
                    else -> "emptyList()"
                }
            )

            JimmerTypeKind.ARRAY -> DefaultValueResult(
                code = when {
                    type.rendered.startsWith("Array<") -> "emptyArray()"
                    type.rendered.endsWith("Array") -> "${type.rendered}Of()"
                    else -> "emptyArray()"
                }
            )

            JimmerTypeKind.DATE_TIME -> {
                val imports = mutableSetOf<String>()
                val code = when (type.rendered) {
                    "LocalDateTime" -> {
                        imports.add("import kotlinx.datetime.TimeZone")
                        imports.add("import kotlinx.datetime.toLocalDateTime")
                        "kotlinx.datetime.Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())"
                    }

                    "LocalDate" -> {
                        imports.add("import kotlinx.datetime.TimeZone")
                        imports.add("import kotlinx.datetime.toLocalDateTime")
                        "kotlinx.datetime.Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date"
                    }

                    "LocalTime" -> {
                        imports.add("import kotlinx.datetime.TimeZone")
                        imports.add("import kotlinx.datetime.toLocalDateTime")
                        "kotlinx.datetime.Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).time"
                    }

                    "Instant" -> "kotlinx.datetime.Clock.System.now()"
                    else -> "TODO()"
                }
                DefaultValueResult(code = code, imports = imports)
            }

            JimmerTypeKind.ENUM -> DefaultValueResult("${type.rendered}.entries.first()")
            JimmerTypeKind.ENTITY -> DefaultValueResult("${type.rendered}()")
            JimmerTypeKind.OTHER -> DefaultValueResult("TODO()")
        }
    }

    /** 识别并标准化 Kotlin/Java 原始数值类型。 */
    private fun mapToKotlinPrimitive(qualifiedName: String?, simpleName: String): String? {
        return when (normalizeTypeToken(qualifiedName ?: simpleName).lowercase()) {
            "kotlin.int", "java.lang.integer", "int", "integer" -> "Int"
            "kotlin.long", "java.lang.long", "long" -> "Long"
            "kotlin.short", "java.lang.short", "short" -> "Short"
            "kotlin.byte", "java.lang.byte", "byte" -> "Byte"
            "kotlin.float", "java.lang.float", "float" -> "Float"
            "kotlin.double", "java.lang.double", "double" -> "Double"
            "kotlin.boolean", "java.lang.boolean", "boolean" -> "Boolean"
            "kotlin.char", "java.lang.character", "char", "character" -> "Char"
            else -> null
        }
    }

    /** 把 Java/Kotlin 时间类型统一映射到 `kotlinx.datetime`。 */
    private fun mapToKotlinxDateTime(qualifiedName: String?, simpleName: String): String? {
        val normalized = normalizeTypeToken(qualifiedName ?: simpleName)
        return when (normalized) {
            "java.time.LocalDate" -> "LocalDate"
            "java.time.LocalDateTime" -> "LocalDateTime"
            "java.time.LocalTime" -> "LocalTime"
            "java.time.Instant" -> "Instant"
            "kotlinx.datetime.LocalDate" -> "LocalDate"
            "kotlinx.datetime.LocalDateTime" -> "LocalDateTime"
            "kotlinx.datetime.LocalTime" -> "LocalTime"
            "kotlinx.datetime.Instant" -> "Instant"
            else -> null
        }
    }

    /** 清洗可能带有泛型残片或可空标记的类型 token。 */
    private fun normalizeTypeToken(typeName: String): String {
        return typeName
            .trim()
            .trimStart('[', '(')
            .trimEnd(']', ')', ',', ';')
            .removeSuffix("?")
            .ifBlank { "Any" }
    }

    /** 判断该限定名是否需要生成 import。 */
    private fun shouldImport(qualifiedName: String): Boolean {
        return !qualifiedName.startsWith("kotlin.") &&
            !qualifiedName.startsWith("java.lang.")
    }
}
