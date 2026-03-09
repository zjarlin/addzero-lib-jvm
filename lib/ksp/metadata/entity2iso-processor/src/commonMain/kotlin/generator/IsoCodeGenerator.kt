package generator

import androidx.room.compiler.processing.XMethodElement
import androidx.room.compiler.processing.XNullability
import androidx.room.compiler.processing.XType
import androidx.room.compiler.processing.XTypeElement
import androidx.room.compiler.processing.isArray
import androidx.room.compiler.processing.isEnum

/**
 * 同构体代码生成器（XProcessing 版本）
 */
object IsoCodeGenerator {
    private const val JIMMER_ENTITY_ANNOTATION = "org.babyfish.jimmer.sql.Entity"

    fun generateIsoCode(entity: XTypeElement, packageName: String): String {
        val propertyModels = collectEntityProperties(entity).map { property ->
            val typeResult = buildIsoType(property.type, packageName)
            val defaultValueResult = defaultValueFor(property, typeResult)
            val contextualAnnotation = if (typeResult.contextual) "@Contextual " else ""
            val nullableSuffix = if (property.nullable) "?" else ""

            PropertyModel(
                code = "    ${contextualAnnotation}val ${property.name}: ${typeResult.rendered}$nullableSuffix = ${defaultValueResult.code}",
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
            |data class ${entity.name}Iso(
            |$props
            |)
        """.trimMargin().trim()
    }

    private data class EntityProperty(
        val name: String,
        val type: XType,
        val nullable: Boolean
    )

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

    private fun collectEntityProperties(entity: XTypeElement): List<EntityProperty> {
        val byName = linkedMapOf<String, EntityProperty>()

        entity.getAllMethods()
            .asSequence()
            .filter { it.isKotlinPropertyGetter() }
            .filter { !it.isStatic() }
            .filter { it.parameters.isEmpty() }
            .forEach { method ->
                val propertyName = method.propertyName ?: normalizeMethodPropertyName(method)
                if (propertyName.isBlank()) {
                    return@forEach
                }
                byName[propertyName] = EntityProperty(
                    name = propertyName,
                    type = method.returnType,
                    nullable = method.returnType.nullability == XNullability.NULLABLE
                )
            }

        if (byName.isNotEmpty()) {
            return byName.values.toList()
        }

        entity.getAllFieldsIncludingPrivateSupers()
            .asSequence()
            .filter { !it.isStatic() }
            .forEach { field ->
                byName[field.name] = EntityProperty(
                    name = field.name,
                    type = field.type,
                    nullable = field.type.nullability == XNullability.NULLABLE
                )
            }

        return byName.values.toList()
    }

    private fun normalizeMethodPropertyName(method: XMethodElement): String {
        val methodName = method.name
        if (methodName.startsWith("get") && methodName.length > 3) {
            return methodName.substring(3).replaceFirstChar { it.lowercase() }
        }
        if (methodName.startsWith("is") && methodName.length > 2) {
            return methodName.substring(2).replaceFirstChar { it.lowercase() }
        }
        return methodName
    }

    private fun buildIsoType(type: XType, currentPackageName: String): IsoTypeResult {
        val typeElement = type.typeElement
        val qualifiedName = typeElement?.qualifiedName
        val simpleName = qualifiedName?.substringAfterLast('.') ?: parseSimpleTypeName(type.asTypeName().toString())

        if (type.isArray()) {
            return buildIsoArrayType(type, currentPackageName)
        }

        if (isCollectionType(qualifiedName, simpleName)) {
            val renderedArgs = type.typeArguments.map { buildIsoType(it, currentPackageName) }
            val rendered = if (renderedArgs.isNotEmpty()) {
                "$simpleName<${renderedArgs.joinToString(", ") { it.rendered }}>"
            } else {
                simpleName
            }
            val imports = renderedArgs.flatMap { it.imports }.toSet()
            return IsoTypeResult(rendered = rendered, imports = imports, kind = IsoTypeKind.COLLECTION)
        }

        mapToKotlinPrimitive(simpleName)?.let { primitive ->
            return IsoTypeResult(rendered = primitive, kind = IsoTypeKind.BASIC)
        }

        if (qualifiedName == "kotlin.String" || qualifiedName == "java.lang.String" || simpleName == "String") {
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
                imports = setOf("import kotlinx.datetime.$mapped") +
                    if (contextual) setOf("import kotlinx.serialization.Contextual") else emptySet(),
                kind = IsoTypeKind.DATE_TIME,
                contextual = contextual,
                rawQualifiedName = qualifiedName
            )
        }

        if (qualifiedName in setOf("java.util.Date", "java.sql.Timestamp")) {
            return IsoTypeResult(rendered = "Long", kind = IsoTypeKind.BASIC, rawQualifiedName = qualifiedName)
        }

        if (typeElement?.isEnum() == true) {
            val imports = qualifiedName
                ?.takeIf { shouldImport(it, currentPackageName) }
                ?.let { setOf("import $it") }
                .orEmpty()
            return IsoTypeResult(rendered = simpleName, imports = imports, kind = IsoTypeKind.ENUM, rawQualifiedName = qualifiedName)
        }

        if (isJimmerEntity(typeElement)) {
            return IsoTypeResult(rendered = "${simpleName}Iso", kind = IsoTypeKind.ENTITY_ISO, rawQualifiedName = qualifiedName)
        }

        val imports = qualifiedName
            ?.takeIf { shouldImport(it, currentPackageName) }
            ?.let { setOf("import $it") }
            .orEmpty()
        return IsoTypeResult(rendered = simpleName, imports = imports, kind = IsoTypeKind.OTHER, rawQualifiedName = qualifiedName)
    }

    private fun buildIsoArrayType(type: XType, currentPackageName: String): IsoTypeResult {
        val qualifiedName = type.typeElement?.qualifiedName
        val simpleName = qualifiedName?.substringAfterLast('.') ?: parseSimpleTypeName(type.asTypeName().toString())

        if (simpleName != "Array" && (qualifiedName?.startsWith("kotlin.") == true)) {
            return IsoTypeResult(rendered = simpleName, kind = IsoTypeKind.ARRAY, rawQualifiedName = qualifiedName)
        }

        val arg = type.typeArguments.firstOrNull()?.let { buildIsoType(it, currentPackageName) }
        val rendered = if (arg != null) "Array<${arg.rendered}>" else "Array<Any>"
        val imports = arg?.imports.orEmpty()
        return IsoTypeResult(rendered = rendered, imports = imports, kind = IsoTypeKind.ARRAY, rawQualifiedName = qualifiedName)
    }

    private fun defaultValueFor(property: EntityProperty, type: IsoTypeResult): DefaultValueResult {
        if (property.nullable) return DefaultValueResult("null")

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
                    type.rendered.endsWith("Array") -> "${type.rendered}Of()"
                    else -> "emptyArray()"
                }
            )

            IsoTypeKind.DATE_TIME -> {
                val imports = mutableSetOf("import kotlin.time.Clock")
                val needsExperimentalTime = true
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
        val normalized = qualifiedName ?: simpleName
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

    private fun isCollectionType(qualifiedName: String?, simpleName: String): Boolean {
        if (qualifiedName == null) {
            return simpleName in setOf("List", "MutableList", "Set", "MutableSet", "Collection", "Map", "MutableMap")
        }
        return qualifiedName in setOf(
            "kotlin.collections.List",
            "kotlin.collections.MutableList",
            "kotlin.collections.Set",
            "kotlin.collections.MutableSet",
            "kotlin.collections.Collection",
            "kotlin.collections.Map",
            "kotlin.collections.MutableMap",
            "java.util.List",
            "java.util.Set",
            "java.util.Collection",
            "java.util.Map"
        )
    }

    private fun isJimmerEntity(typeElement: XTypeElement?): Boolean {
        return typeElement?.getAllAnnotations()?.any { it.qualifiedName == JIMMER_ENTITY_ANNOTATION } == true
    }

    private fun parseSimpleTypeName(typeName: String): String {
        return typeName
            .substringBefore('<')
            .substringAfterLast('.')
            .removeSuffix("?")
            .trim()
            .ifBlank { "Any" }
    }

    private fun shouldImport(qualifiedName: String, currentPackageName: String): Boolean {
        val packageName = qualifiedName.substringBeforeLast('.', missingDelimiterValue = "")
        if (packageName.isBlank()) return false
        return packageName != currentPackageName &&
            !qualifiedName.startsWith("kotlin.") &&
            !qualifiedName.startsWith("java.lang.")
    }
}
