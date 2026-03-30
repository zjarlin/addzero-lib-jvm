package generator

import site.addzero.ksp.metadata.jimmer.entity.spi.JimmerEntityMeta
import site.addzero.ksp.metadata.jimmer.entity.spi.JimmerPropertyMeta
import site.addzero.ksp.metadata.jimmer.entity.spi.JimmerTypeKind
import site.addzero.ksp.metadata.jimmer.entity.spi.JimmerTypeRef

object IsoCodeGenerator {
    fun generateIsoCode(
        entity: JimmerEntityMeta,
        packageName: String,
        classSuffix: String
    ): String {
        val propertyModels = entity.properties.map { property ->
            val typeResult = buildIsoType(property.type, classSuffix)
            val defaultValueResult = defaultValueFor(property, typeResult)
            val contextualAnnotation = if (typeResult.contextual) "@Contextual " else ""
            val nullableSuffix = if (property.type.nullable) "?" else ""

            PropertyModel(
                code = "    ${contextualAnnotation}val ${property.name}: ${typeResult.rendered}$nullableSuffix = ${defaultValueResult.code}",
                imports = typeResult.imports + defaultValueResult.imports,
                needsExperimentalTimeOptIn = defaultValueResult.needsExperimentalTimeOptIn
            )
        }

        val props = propertyModels.joinToString(",\n") { it.code }
        val imports = propertyModels.flatMap { it.imports }.toMutableSet()
        imports.add("import kotlinx.serialization.Serializable")

        val optimizedImports = imports.sorted().joinToString("\n")
        val isoClassName = "${entity.simpleName}$classSuffix"

        return """
            |package $packageName
            |
            |${optimizedImports.takeIf { it.isNotBlank() } ?: ""}
            |
            |@Serializable
            |data class $isoClassName(
            |$props
            |)
        """.trimMargin().trim()
    }

    private data class PropertyModel(
        val code: String,
        val imports: Set<String>,
        val needsExperimentalTimeOptIn: Boolean
    )

    private data class IsoTypeResult(
        val rendered: String,
        val imports: Set<String> = emptySet(),
        val kind: JimmerTypeKind = JimmerTypeKind.OTHER,
        val contextual: Boolean = false
    )

    private data class DefaultValueResult(
        val code: String,
        val imports: Set<String> = emptySet(),
        val needsExperimentalTimeOptIn: Boolean = false
    )

    private fun buildIsoType(type: JimmerTypeRef, classSuffix: String): IsoTypeResult {
        if (type.kind == JimmerTypeKind.ARRAY) {
            return buildIsoArrayType(type, classSuffix)
        }

        if (type.kind == JimmerTypeKind.COLLECTION) {
            val renderedArgs = type.typeArguments.map { buildIsoType(it, classSuffix) }
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
                kind = JimmerTypeKind.BASIC
            )
        }

        mapToKotlinxDateTime(type.qualifiedName, type.simpleName)?.let { mapped ->
            val contextual = mapped in setOf("LocalDateTime", "LocalDate", "Instant")
            return IsoTypeResult(
                rendered = mapped,
                imports = setOf("import kotlinx.datetime.$mapped") +
                    if (contextual) setOf("import kotlinx.serialization.Contextual") else emptySet(),
                kind = JimmerTypeKind.DATE_TIME,
                contextual = contextual
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

    private fun buildIsoArrayType(type: JimmerTypeRef, classSuffix: String): IsoTypeResult {
        if (type.simpleName != "Array" && (type.qualifiedName?.startsWith("kotlin.") == true)) {
            return IsoTypeResult(rendered = type.simpleName, kind = JimmerTypeKind.ARRAY)
        }

        val arg = type.typeArguments.firstOrNull()?.let { buildIsoType(it, classSuffix) }
        val rendered = if (arg != null) "Array<${arg.rendered}>" else "Array<Any>"
        val imports = arg?.imports.orEmpty()
        return IsoTypeResult(rendered = rendered, imports = imports, kind = JimmerTypeKind.ARRAY)
    }

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
                val imports = mutableSetOf("import kotlinx.datetime.Clock")
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
                DefaultValueResult(code = code, imports = imports)
            }

            JimmerTypeKind.ENUM -> DefaultValueResult("${type.rendered}.entries.first()")
            JimmerTypeKind.ENTITY -> DefaultValueResult("${type.rendered}()")
            JimmerTypeKind.OTHER -> DefaultValueResult("TODO()")
        }
    }

    private fun mapToKotlinPrimitive(qualifiedName: String?, simpleName: String): String? {
        return when ((qualifiedName ?: simpleName).lowercase()) {
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

    private fun shouldImport(qualifiedName: String): Boolean {
        return !qualifiedName.startsWith("kotlin.") &&
            !qualifiedName.startsWith("java.lang.")
    }
}
