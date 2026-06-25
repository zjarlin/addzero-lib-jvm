package site.addzero.jimmer.lowquery.processor

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies

internal class JimmerLowQueryGenerator(
    private val codeGenerator: CodeGenerator,
) {
    fun generate(
        entities: Set<LowQueryEntityMeta>,
        generatedPackage: String?,
        springComponentAvailable: Boolean = false,
    ) {
        entities.groupBy { it.outputPackage(generatedPackage) }
            .forEach { (packageName, packageEntities) ->
                val fileName = packageName.substringAfterLast('.').replaceFirstChar(Char::uppercase) + "JimmerLowQueries"
                createFile(packageName, fileName).use { stream ->
                    val code = buildFile(packageName, packageEntities.sortedBy { it.qualifiedName }, springComponentAvailable)
                    stream.write(code.toByteArray())
                }
            }
    }

    private fun LowQueryEntityMeta.outputPackage(generatedPackage: String?): String {
        return generatedPackage?.takeIf { it.isNotBlank() } ?: "$packageName.generated.lowquery"
    }

    private fun createFile(packageName: String, fileName: String) =
        codeGenerator.createNewFile(Dependencies.ALL_FILES, packageName, fileName, "kt")

    private fun buildFile(
        packageName: String,
        entities: List<LowQueryEntityMeta>,
        springComponentAvailable: Boolean,
    ): String {
        return buildString {
            appendLine("@file:Suppress(\"unused\")")
            appendLine()
            appendLine("package $packageName")
            appendLine()
            appendLine("import org.babyfish.jimmer.ImmutableObjects")
            appendLine("import org.babyfish.jimmer.sql.ast.LikeMode")
            appendLine("import org.babyfish.jimmer.sql.kt.KSqlClient")
            appendLine("import org.babyfish.jimmer.sql.kt.ast.expression.asc")
            appendLine("import org.babyfish.jimmer.sql.kt.ast.expression.desc")
            appendLine("import org.babyfish.jimmer.sql.kt.ast.expression.`eq?`")
            appendLine("import org.babyfish.jimmer.sql.kt.ast.expression.`ge?`")
            appendLine("import org.babyfish.jimmer.sql.kt.ast.expression.`gt?`")
            appendLine("import org.babyfish.jimmer.sql.kt.ast.expression.`ilike?`")
            appendLine("import org.babyfish.jimmer.sql.kt.ast.expression.`le?`")
            appendLine("import org.babyfish.jimmer.sql.kt.ast.expression.`like?`")
            appendLine("import org.babyfish.jimmer.sql.kt.ast.expression.`lt?`")
            appendLine("import org.babyfish.jimmer.sql.kt.ast.expression.`ne?`")
            appendLine("import org.babyfish.jimmer.sql.kt.ast.expression.`valueIn?`")
            appendLine("import org.babyfish.jimmer.sql.kt.ast.expression.`valueNotIn?`")
            appendLine("import org.babyfish.jimmer.sql.kt.ast.query.KConfigurableRootQuery")
            appendLine("import org.babyfish.jimmer.sql.kt.ast.query.KMutableRootQuery")
            appendLine("import org.babyfish.jimmer.sql.kt.ast.table.KNonNullTable")
            if (springComponentAvailable) {
                appendLine("import org.springframework.stereotype.Component")
                appendLine("import site.addzero.jimmer.lowquery.runtime.JimmerLowQueryProvider")
                appendLine("import kotlin.reflect.KClass")
            }
            entities.flatMap { entity ->
                listOf(entity.qualifiedName, "${entity.packageName}.fetchBy") +
                    entity.params.map { param -> "${entity.packageName}.${param.propertyName.escapeIdentifier()}" } +
                    entity.orders.map { order -> "${entity.packageName}.${order.propertyName.escapeIdentifier()}" }
            }
                .distinct()
                .sorted()
                .forEach { appendLine("import $it") }
            appendLine()
            entities.forEachIndexed { index, entity ->
                append(entity.buildFunction())
                appendLine()
                appendLine()
                append(entity.buildApplyFunction())
                appendLine()
                appendLine()
                append(entity.buildClientFunction())
                if (springComponentAvailable) {
                    appendLine()
                    appendLine()
                    append(entity.buildSpringProvider())
                }
                if (index != entities.lastIndex) {
                    appendLine()
                    appendLine()
                }
            }
        }
    }

    private fun LowQueryEntityMeta.buildFunction(): String {
        val parameterLines = params.joinToString(",\n") { param ->
            "    ${param.renderParameter()}"
        }
        val whereLines = params.joinToString("\n") { it.buildWhereLine() }
        val orderByLine = buildOrderByLine("    ")
        val returnType = "KConfigurableRootQuery<KNonNullTable<$simpleName>, $simpleName>"
        return buildString {
            if (params.isEmpty()) {
                appendLine("${visibility.code} fun KMutableRootQuery.ForEntity<$simpleName>.${functionName.escapeIdentifier()}(): $returnType {")
            } else {
                appendLine("${visibility.code} fun KMutableRootQuery.ForEntity<$simpleName>.${functionName.escapeIdentifier()}(")
                appendLine(parameterLines)
                appendLine("): $returnType {")
            }
            if (whereLines.isNotEmpty()) {
                appendLine(whereLines)
            }
            if (orderByLine != null) {
                appendLine(orderByLine)
            }
            appendLine("    return ${buildSelectExpression()}")
            append("}")
        }
    }

    private fun LowQueryEntityMeta.buildClientFunction(): String {
        val annotation = "@JvmName(\"${clientFunctionName}For${simpleName}ByEntity\")"
        return buildString {
            appendLine(annotation)
            appendLine("${clientVisibility.code} fun KSqlClient.${clientFunctionName.escapeIdentifier()}(")
            appendLine("    entity: $simpleName")
            appendLine("): KConfigurableRootQuery<KNonNullTable<$simpleName>, $simpleName> {")
            appendLine("    return createQuery($simpleName::class) {")
            appendLine("        applyLowQuery(entity)")
            appendLine("        ${buildSelectExpression()}")
            appendLine("    }")
            append("}")
        }
    }

    private fun LowQueryEntityMeta.buildApplyFunction(): String {
        val whereLines = params.joinToString("\n") { param -> param.buildEntityWhereBlock() }
        val orderByLine = buildOrderByLine("    ")
        return buildString {
            appendLine("private fun KMutableRootQuery.ForEntity<$simpleName>.applyLowQuery(")
            appendLine("    entity: $simpleName")
            appendLine(") {")
            if (whereLines.isNotEmpty()) {
                appendLine(whereLines)
            }
            if (orderByLine != null) {
                appendLine(orderByLine)
            }
            append("}")
        }
    }

    private fun LowQueryEntityMeta.buildSpringProvider(): String {
        val providerName = "${simpleName}JimmerLowQueryProvider"
        val beanName = "$qualifiedName.$providerName"
        val parameterMap = params.joinToString(", ") { param ->
            "\"${param.propertyName.escapeString()}\" to \"${param.parameterName.escapeString()}\""
        }
        return buildString {
            appendLine("@Component(\"${beanName.escapeString()}\")")
            appendLine("public class $providerName : JimmerLowQueryProvider<$simpleName> {")
            appendLine("    override val entityType: KClass<$simpleName> = $simpleName::class")
            appendLine()
            appendLine("    override val parameterNames: Map<String, String> = mapOf($parameterMap)")
            appendLine()
            appendLine("    override val hasOrderBy: Boolean = ${orders.isNotEmpty()}")
            appendLine()
            appendLine("    override fun apply(")
            appendLine("        query: KMutableRootQuery.ForEntity<$simpleName>,")
            appendLine("        entity: $simpleName")
            appendLine("    ) {")
            appendLine("        query.applyLowQuery(entity)")
            appendLine("    }")
            append("}")
        }
    }

    private fun LowQueryEntityMeta.buildSelectExpression(): String {
        return when (fetcher) {
            LowQueryFetcher.ALL_SCALAR_FIELDS -> "select(table.fetchBy { allScalarFields() })"
            LowQueryFetcher.ALL_TABLE_FIELDS -> "select(table.fetchBy { allTableFields() })"
            LowQueryFetcher.TABLE -> "select(table)"
        }
    }

    private fun LowQueryEntityMeta.buildOrderByLine(indent: String): String? {
        if (orders.isEmpty()) {
            return null
        }
        val expressions = orders.joinToString(", ") { order ->
            val functionName = when (order.direction) {
                LowQueryOrderDirection.ASC -> "asc"
                LowQueryOrderDirection.DESC -> "desc"
            }
            "table.${order.propertyName.escapeIdentifier()}.$functionName()"
        }
        return "${indent}orderBy($expressions)"
    }

    private fun LowQueryParamMeta.buildWhereLine(): String {
        val property = "table.${propertyName.escapeIdentifier()}"
        val parameter = parameterName.escapeIdentifier()
        val condition = when (operator) {
            LowQueryOperator.EQ -> "$property `eq?` $parameter"
            LowQueryOperator.NE -> "$property `ne?` $parameter"
            LowQueryOperator.LIKE -> "$property.`ilike?`($parameter, LikeMode.ANYWHERE)"
            LowQueryOperator.STARTS_WITH -> "$property.`like?`($parameter, LikeMode.START)"
            LowQueryOperator.ENDS_WITH -> "$property.`like?`($parameter, LikeMode.END)"
            LowQueryOperator.GT -> "$property `gt?` $parameter"
            LowQueryOperator.GE -> "$property `ge?` $parameter"
            LowQueryOperator.LT -> "$property `lt?` $parameter"
            LowQueryOperator.LE -> "$property `le?` $parameter"
            LowQueryOperator.IN -> "$property `valueIn?` $parameter"
            LowQueryOperator.NOT_IN -> "$property `valueNotIn?` $parameter"
        }
        return "    where($condition)"
    }

    private fun LowQueryParamMeta.buildEntityWhereBlock(): String {
        val property = "table.${propertyName.escapeIdentifier()}"
        val entityValue = entityValueExpression()
        val condition = when (operator) {
            LowQueryOperator.EQ -> "$property `eq?` $entityValue"
            LowQueryOperator.NE -> "$property `ne?` $entityValue"
            LowQueryOperator.LIKE -> "$property.`ilike?`($entityValue, LikeMode.ANYWHERE)"
            LowQueryOperator.STARTS_WITH -> "$property.`like?`($entityValue, LikeMode.START)"
            LowQueryOperator.ENDS_WITH -> "$property.`like?`($entityValue, LikeMode.END)"
            LowQueryOperator.GT -> "$property `gt?` $entityValue"
            LowQueryOperator.GE -> "$property `ge?` $entityValue"
            LowQueryOperator.LT -> "$property `lt?` $entityValue"
            LowQueryOperator.LE -> "$property `le?` $entityValue"
            LowQueryOperator.IN -> "$property `valueIn?` $entityValue"
            LowQueryOperator.NOT_IN -> "$property `valueNotIn?` $entityValue"
        }
        return buildString {
            appendLine("        if (ImmutableObjects.isLoaded(entity, \"${propertyName.escapeString()}\")) {")
            appendLine("            where($condition)")
            append("        }")
        }
    }

    private fun LowQueryParamMeta.entityValueExpression(): String {
        val property = "entity.${propertyName.escapeIdentifier()}"
        return when (operator) {
            LowQueryOperator.IN,
            LowQueryOperator.NOT_IN -> "listOfNotNull($property)"

            else -> property
        }
    }

    private fun LowQueryParamMeta.renderParameter(): String {
        val defaultValue = if (nullable) " = null" else ""
        return "${parameterName.escapeIdentifier()}: $typeName$defaultValue"
    }

    private fun String.escapeIdentifier(): String {
        if (this in KOTLIN_KEYWORDS) {
            return "`$this`"
        }
        return this
    }

    private fun String.escapeString(): String =
        replace("\\", "\\\\")
            .replace("\"", "\\\"")

    private companion object {
        private val KOTLIN_KEYWORDS = setOf(
            "as",
            "break",
            "class",
            "continue",
            "do",
            "else",
            "false",
            "for",
            "fun",
            "if",
            "in",
            "interface",
            "is",
            "null",
            "object",
            "package",
            "return",
            "super",
            "this",
            "throw",
            "true",
            "try",
            "typealias",
            "typeof",
            "val",
            "var",
            "when",
            "while",
        )
    }
}
