package generator

import com.google.devtools.ksp.processing.KSPLogger
import site.addzero.lsi.clazz.LsiClass
import site.addzero.lsi.field.LsiField
import site.addzero.lsi.jimmer.isJimmerEntity
import site.addzero.lsi.type.LsiType

/**
 * 同构体代码生成器
 * 负责将 LSI 实体结构转换为同构体 Kotlin 代码
 */
class IsoCodeGenerator() {

    /**
     * 生成同构体代码
     */
    fun generateIsoCode(entity: LsiClass, packageName: String): String {
        val propertyModels = entity.fields.mapNotNull { field ->
            val name = field.name ?: return@mapNotNull null
            val type = field.type
            val typeResult = type?.let { buildIsoType(it, packageName) } ?: IsoTypeResult("Any")
            val defaultValueResult = defaultValueFor(field, typeResult)

            val contextualAnnotation = if (typeResult.contextual) "@Contextual " else ""
            val nullableSuffix = if (field.isNullable) "?" else ""

            PropertyModel(
                code = "    ${contextualAnnotation}val $name: ${typeResult.rendered}$nullableSuffix = ${defaultValueResult.code}",
                imports = typeResult.imports + defaultValueResult.imports,
                needsExperimentalTimeOptIn = defaultValueResult.needsExperimentalTimeOptIn
            )
        }

        val props = propertyModels.joinToString(",\n") { it.code }
        val imports = propertyModels.flatMap { it.imports }.toMutableSet()

        val needsExperimentalTimeOptIn = propertyModels.any { it.needsExperimentalTimeOptIn }
        if (needsExperimentalTimeOptIn) {
            imports.add("import kotlin.time.ExperimentalTime")
        }

        val optimizedImports = imports.sorted().joinToString("\n")
        val fileOptIn = if (needsExperimentalTimeOptIn) "@file:OptIn(ExperimentalTime::class)\n\n" else ""

        return """
            |$fileOptIn
            |package $packageName
            |
            |${optimizedImports.takeIf { it.isNotBlank() } ?: ""}
            |
            |data class ${entity.simpleName}Iso(
            |$props
            |)
        """.trimMargin().trim()
    }

    /**
     * 生成属性代码
     */
    private data class PropertyModel(
        val code: String,
        val imports: Set<String>,
        val needsExperimentalTimeOptIn: Boolean
    )

    private data class IsoTypeResult(
        val rendered: String,
        val imports: Set<String> = emptySet(),
        val kind: IsoTypeKind = IsoTypeKind.OTHER,
        val contextual: Boolean = false,
        val rawQualifiedName: String? = null
    )

    private enum class IsoTypeKind {
        BASIC,
        DATE_TIME,
        ENUM,
        ENTITY_ISO,
        COLLECTION,
        ARRAY,
        OTHER
    }

    private data class DefaultValueResult(
        val code: String,
        val imports: Set<String> = emptySet(),
        val needsExperimentalTimeOptIn: Boolean = false
    )

    private fun buildIsoType(type: LsiType, currentPackageName: String): IsoTypeResult {
        if (type.isArray) {
            return buildIsoArrayType(type, currentPackageName)
        }

        if (type.isCollectionType) {
            val rawName = type.simpleName ?: type.presentableText ?: type.qualifiedName ?: "List"
            val renderedArgs = type.typeParameters.map { buildIsoType(it, currentPackageName) }
            val rendered = if (renderedArgs.isNotEmpty()) {
                "$rawName<${renderedArgs.joinToString(", ") { it.rendered }}>"
            } else {
                rawName
            }
            val imports = renderedArgs.flatMap { it.imports }.toSet()
            return IsoTypeResult(rendered = rendered, imports = imports, kind = IsoTypeKind.COLLECTION)
        }

        val qualifiedName = type.qualifiedName ?: type.lsiClass?.qualifiedName
        val simpleName = type.simpleName ?: qualifiedName?.substringAfterLast('.') ?: "Any"

        mapToKotlinPrimitive(simpleName)?.let { primitive ->
            return IsoTypeResult(rendered = primitive, kind = IsoTypeKind.BASIC)
        }
        if (simpleName == "String" || qualifiedName == "kotlin.String" || qualifiedName == "java.lang.String") {
            return IsoTypeResult(rendered = "String", kind = IsoTypeKind.BASIC)
        }
        if (qualifiedName == "java.math.BigDecimal" || simpleName == "BigDecimal") {
            return IsoTypeResult(
                rendered = "BigDecimal",
                imports = setOf("import java.math.BigDecimal"),
                kind = IsoTypeKind.BASIC,
                rawQualifiedName = "java.math.BigDecimal"
            )
        }

        mapToKotlinxDateTime(qualifiedName, simpleName)?.let { mapped ->
            val contextual = mapped in setOf("LocalDateTime", "LocalDate", "Instant")
            return IsoTypeResult(
                rendered = mapped,
                imports = setOf("import kotlinx.datetime.$mapped") + if (contextual) setOf("import kotlinx.serialization.Contextual") else emptySet(),
                kind = IsoTypeKind.DATE_TIME,
                contextual = contextual,
                rawQualifiedName = qualifiedName
            )
        }

        if (qualifiedName in setOf("java.util.Date", "java.sql.Timestamp")) {
            return IsoTypeResult(rendered = "Long", kind = IsoTypeKind.BASIC, rawQualifiedName = qualifiedName)
        }

        val isEnum = type.lsiClass?.isEnum == true
        if (isEnum) {
            val imports = qualifiedName
                ?.takeIf { shouldImport(it, currentPackageName) }
                ?.let { setOf("import $it") }
                .orEmpty()
            return IsoTypeResult(rendered = simpleName, imports = imports, kind = IsoTypeKind.ENUM, rawQualifiedName = qualifiedName)
        }

        if (type.lsiClass?.isJimmerEntity ?: false) {
            return IsoTypeResult(rendered = "${simpleName}Iso", kind = IsoTypeKind.ENTITY_ISO, rawQualifiedName = qualifiedName)
        }

        val imports = qualifiedName
            ?.takeIf { shouldImport(it, currentPackageName) }
            ?.let { setOf("import $it") }
            .orEmpty()

        return IsoTypeResult(rendered = simpleName, imports = imports, kind = IsoTypeKind.OTHER, rawQualifiedName = qualifiedName)
    }

    private fun buildIsoArrayType(type: LsiType, currentPackageName: String): IsoTypeResult {
        val qualifiedName = type.qualifiedName ?: type.lsiClass?.qualifiedName
        val simpleName = type.simpleName ?: qualifiedName?.substringAfterLast('.') ?: "Array"

        if (simpleName != "Array" && (qualifiedName?.startsWith("kotlin.") == true)) {
            return IsoTypeResult(rendered = simpleName, kind = IsoTypeKind.ARRAY, rawQualifiedName = qualifiedName)
        }

        val arg = type.componentType?.let { buildIsoType(it, currentPackageName) }
        val rendered = if (arg != null) "Array<${arg.rendered}>" else "Array<Any>"
        val imports = arg?.imports.orEmpty()
        return IsoTypeResult(rendered = rendered, imports = imports, kind = IsoTypeKind.ARRAY, rawQualifiedName = qualifiedName)
    }

    private fun defaultValueFor(field: LsiField, type: IsoTypeResult): DefaultValueResult {
        if (field.isNullable) return DefaultValueResult("null")

        return when (type.kind) {
            IsoTypeKind.BASIC -> when (type.rendered) {
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

            IsoTypeKind.COLLECTION -> DefaultValueResult(
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

            IsoTypeKind.ARRAY -> DefaultValueResult(
                code = when {
                    type.rendered.startsWith("Array<") -> "emptyArray()"
                    type.rendered.endsWith("Array") -> "${type.rendered.replace("<", "").replace(">", "").replace(",", "")}Of()"
                    else -> "emptyArray()"
                }
            )

            IsoTypeKind.DATE_TIME -> {
                val imports = mutableSetOf<String>()
                val needsExperimentalTime = true
                imports.add("import kotlin.time.Clock")

                val code = when (type.rendered) {
                    "LocalDateTime" -> {
                        imports.add("import kotlinx.datetime.TimeZone")
                        imports.add("import kotlinx.datetime.toLocalDateTime")
                        "Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())"
                    }

                    "LocalDate" -> {
                        imports.add("import kotlinx.datetime.TimeZone")
                        imports.add("import kotlinx.datetime.toLocalDateTime")
                        "Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date"
                    }

                    "LocalTime" -> {
                        imports.add("import kotlinx.datetime.TimeZone")
                        imports.add("import kotlinx.datetime.toLocalDateTime")
                        "Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).time"
                    }

                    "Instant" -> "Clock.System.now()"
                    else -> "TODO()"
                }

                DefaultValueResult(code = code, imports = imports, needsExperimentalTimeOptIn = needsExperimentalTime)
            }

            IsoTypeKind.ENUM -> DefaultValueResult("${type.rendered}.entries.first()")
            IsoTypeKind.ENTITY_ISO -> DefaultValueResult("${type.rendered}()")
            IsoTypeKind.OTHER -> DefaultValueResult("TODO()")
        }
    }

    private fun mapToKotlinPrimitive(simpleName: String): String? {
        return when (simpleName.lowercase()) {
            "int", "integer" -> "Int"
            "long" -> "Long"
            "short" -> "Short"
            "byte" -> "Byte"
            "float" -> "Float"
            "double" -> "Double"
            "boolean" -> "Boolean"
            "char", "character" -> "Char"
            else -> null
        }
    }

    private fun mapToKotlinxDateTime(qualifiedName: String?, simpleName: String): String? {
        return when {
            qualifiedName == null -> null

            qualifiedName.startsWith("java.time.") || qualifiedName.startsWith("kotlinx.datetime.") -> when (simpleName) {
                "LocalDateTime", "LocalDate", "LocalTime", "Instant" -> simpleName
                "ZonedDateTime", "OffsetDateTime" -> "LocalDateTime"
                else -> null
            }

            qualifiedName == "java.sql.Date" -> "LocalDate"
            qualifiedName == "java.sql.Time" -> "LocalTime"
            qualifiedName == "java.sql.Timestamp" -> "LocalDateTime"
            else -> null
        }
    }

    private fun isJimmerEntityType(type: LsiType, qualifiedName: String?): Boolean {

        val lsiClass = type.lsiClass
        val hasEntityAnno = lsiClass?.annotations?.any {
            it.qualifiedName == "org.babyfish.jimmer.sql.Entity" || it.simpleName == "Entity"
        } == true
        if (hasEntityAnno) return true

        val qName = qualifiedName ?: return false
        return looksLikeJimmerEntityByPackage(qName)
    }

    private fun looksLikeJimmerEntityByPackage(qualifiedType: String): Boolean {
        return qualifiedType.contains(".entity.") ||
                qualifiedType.contains(".modules.") ||
                qualifiedType.startsWith("site.addzero.web.modules.")
    }

    private fun shouldImport(qualifiedType: String, currentPackageName: String): Boolean {
        if (qualifiedType.startsWith("$currentPackageName.")) return false
        if (isKotlinBuiltinType(qualifiedType)) return false
        if (looksLikeJimmerEntityByPackage(qualifiedType)) return false
        return true
    }

    private fun isKotlinBuiltinType(typeName: String): Boolean {
        if (!typeName.contains('.')) {
            return typeName in setOf(
                "String", "Int", "Long", "Boolean", "Double", "Float", "Short", "Byte", "Char", "Any",
                "List", "Set", "Map", "MutableList", "MutableSet", "MutableMap"
            )
        }
        return typeName.startsWith("kotlin.") ||
                typeName.startsWith("kotlin.collections.") ||
                typeName.startsWith("kotlin.ranges.") ||
                typeName.startsWith("kotlin.sequences.") ||
                typeName.startsWith("kotlin.io.") ||
                typeName.startsWith("java.lang.")
    }
}
