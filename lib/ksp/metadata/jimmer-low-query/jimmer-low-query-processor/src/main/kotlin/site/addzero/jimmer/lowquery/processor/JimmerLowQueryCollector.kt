package site.addzero.jimmer.lowquery.processor

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.validate

internal const val LOW_QUERY_ANNOTATION = "site.addzero.jimmer.lowquery.annotation.JimmerLowQuery"
internal const val LOW_QUERY_PARAM_ANNOTATION = "site.addzero.jimmer.lowquery.annotation.JimmerLowQueryParam"
private const val JIMMER_ENTITY_ANNOTATION = "org.babyfish.jimmer.sql.Entity"
private const val ANNOTATION_PACKAGE = "site.addzero.jimmer.lowquery.annotation"
private const val ORDER_BY_ASC_ANNOTATION = "$ANNOTATION_PACKAGE.OrderByAsc"
private const val ORDER_BY_DESC_ANNOTATION = "$ANNOTATION_PACKAGE.OrderByDesc"

internal data class LowQueryCollectResult(
    val entities: Set<LowQueryEntityMeta>,
    val deferred: List<KSAnnotated>,
    val hasErrors: Boolean,
)

internal class JimmerLowQueryCollector(
    private val resolver: Resolver,
    private val logger: KSPLogger,
) {
    private var hasErrors = false

    fun collect(): LowQueryCollectResult {
        val deferred = mutableListOf<KSAnnotated>()
        val entitySymbols = linkedSetOf<KSClassDeclaration>()
        resolver.getSymbolsWithAnnotation(LOW_QUERY_ANNOTATION)
            .forEach { symbol ->
                if (!symbol.validate()) {
                    deferred += symbol
                    return@forEach
                }
                val entity = symbol as? KSClassDeclaration
                if (entity == null) {
                    error("@JimmerLowQuery 只能标记 Jimmer 实体接口。", symbol)
                    return@forEach
                }
                entitySymbols += entity
            }
        collectEntitiesFromPropertyAnnotations(LOW_QUERY_FIELD_ANNOTATIONS, deferred, entitySymbols)
        collectEntitiesFromPropertyAnnotations(LOW_QUERY_ORDER_ANNOTATIONS, deferred, entitySymbols)
        val entities = entitySymbols
            .mapNotNull { entity -> collectEntity(entity) }
            .toSet()
        return LowQueryCollectResult(
            entities = entities,
            deferred = deferred,
            hasErrors = hasErrors,
        )
    }

    private fun collectEntitiesFromPropertyAnnotations(
        annotationNames: Set<String>,
        deferred: MutableList<KSAnnotated>,
        entitySymbols: MutableSet<KSClassDeclaration>,
    ) {
        annotationNames.forEach { annotationName ->
            resolver.getSymbolsWithAnnotation(annotationName)
                .forEach { symbol ->
                    if (!symbol.validate()) {
                        deferred += symbol
                        return@forEach
                    }
                    val property = symbol as? KSPropertyDeclaration
                    if (property == null) {
                        error("低代码查询注解只能标记实体字段。", symbol)
                        return@forEach
                    }
                    val entity = property.parentDeclaration as? KSClassDeclaration
                    if (entity == null) {
                        error("低代码查询注解只能标记实体字段。", property)
                        return@forEach
                    }
                    if (!entity.hasAnnotation(JIMMER_ENTITY_ANNOTATION)) {
                        if (property.isGeneratedSource()) {
                            return@forEach
                        }
                        error("低代码查询注解只能标记 Jimmer 实体字段。", property)
                        return@forEach
                    }
                    entitySymbols += entity
                }
        }
    }

    private fun collectEntity(entity: KSClassDeclaration): LowQueryEntityMeta? {
        if (!entity.hasAnnotation(JIMMER_ENTITY_ANNOTATION)) {
            error("@JimmerLowQuery 只能用于带 @Entity 的 Jimmer 实体。", entity)
            return null
        }
        val annotation = entity.findAnnotation(LOW_QUERY_ANNOTATION)
        val packageName = entity.packageName.asString()
        val simpleName = entity.simpleName.asString()
        val qualifiedName = entity.qualifiedName?.asString()
        if (qualifiedName.isNullOrBlank()) {
            error("无法解析实体 $simpleName 的全限定名。", entity)
            return null
        }
        val properties = entity.getAllProperties().toList()
        val params = properties
            .mapNotNull { property -> collectParam(property) }
        val orders = properties
            .mapNotNull { property -> collectOrder(property) }
            .sortedWith(compareBy<LowQueryOrderMeta> { it.priority }.thenBy { it.propertyName })
        if (params.isEmpty() && orders.isEmpty()) {
            error("$qualifiedName 至少需要一个 @Eq/@Like/@In、@JimmerLowQueryParam 或 @OrderByAsc/@OrderByDesc 字段。", entity)
            return null
        }
        return LowQueryEntityMeta(
            packageName = packageName,
            simpleName = simpleName,
            qualifiedName = qualifiedName,
            functionName = annotation?.stringValue("functionName")?.takeIf { it.isNotBlank() } ?: "query",
            clientFunctionName = annotation?.stringValue("clientFunctionName")?.takeIf { it.isNotBlank() }
                ?: "createLowQuery",
            visibility = annotation?.enumValue("visibility", LowQueryVisibility.PUBLIC) ?: LowQueryVisibility.PUBLIC,
            clientVisibility = annotation?.enumValue("clientVisibility", LowQueryVisibility.PUBLIC)
                ?: LowQueryVisibility.PUBLIC,
            fetcher = annotation?.enumValue("fetcher", LowQueryFetcher.ALL_SCALAR_FIELDS)
                ?: LowQueryFetcher.ALL_SCALAR_FIELDS,
            params = params,
            orders = orders,
        )
    }

    private fun collectParam(property: KSPropertyDeclaration): LowQueryParamMeta? {
        val fieldAnnotations = property.findLowQueryFieldAnnotations()
        if (fieldAnnotations.size > 1) {
            error("字段 ${property.simpleName.asString()} 只能标记一个低代码查询 where 注解。", property)
            return null
        }
        val annotation = fieldAnnotations.singleOrNull() ?: return null
        val propertyName = property.simpleName.asString()
        val operator = annotation.lowQueryOperator()
        val parameterName = annotation.stringValue("name")
            ?.takeIf { it.isNotBlank() }
            ?: propertyName.defaultParameterName(operator)
        val type = property.type.resolve()
        val nullable = annotation.booleanValue("nullable") ?: false
        return LowQueryParamMeta(
            propertyName = propertyName,
            parameterName = parameterName,
            typeName = type.renderParamType(operator, nullable),
            operator = operator,
            nullable = nullable,
        )
    }

    private fun collectOrder(property: KSPropertyDeclaration): LowQueryOrderMeta? {
        val orderAnnotations = property.findLowQueryOrderAnnotations()
        if (orderAnnotations.size > 1) {
            error("字段 ${property.simpleName.asString()} 只能标记一个低代码查询排序注解。", property)
            return null
        }
        val annotation = orderAnnotations.singleOrNull() ?: return null
        val qualifiedName = annotation.annotationType.resolve().declaration.qualifiedName?.asString()
        val direction = when (qualifiedName) {
            ORDER_BY_ASC_ANNOTATION -> LowQueryOrderDirection.ASC
            ORDER_BY_DESC_ANNOTATION -> LowQueryOrderDirection.DESC
            else -> return null
        }
        return LowQueryOrderMeta(
            propertyName = property.simpleName.asString(),
            direction = direction,
            priority = annotation.intValue("priority") ?: 0,
        )
    }

    private fun KSAnnotated.findLowQueryFieldAnnotations(): List<KSAnnotation> {
        return annotations.filter { annotation ->
            annotation.annotationType.resolve().declaration.qualifiedName?.asString() in LOW_QUERY_FIELD_ANNOTATIONS
        }.toList()
    }

    private fun KSAnnotated.findLowQueryOrderAnnotations(): List<KSAnnotation> {
        return annotations.filter { annotation ->
            annotation.annotationType.resolve().declaration.qualifiedName?.asString() in LOW_QUERY_ORDER_ANNOTATIONS
        }.toList()
    }

    private fun KSAnnotated.findAnnotation(qualifiedName: String): KSAnnotation? {
        return annotations.firstOrNull { annotation ->
            annotation.annotationType.resolve().declaration.qualifiedName?.asString() == qualifiedName
        }
    }

    private fun KSAnnotated.hasAnnotation(qualifiedName: String): Boolean {
        return findAnnotation(qualifiedName) != null
    }

    private fun KSPropertyDeclaration.isGeneratedSource(): Boolean {
        val path = containingFile?.filePath ?: return false
        return path.contains("/build/generated/") || path.contains("\\build\\generated\\")
    }

    private fun KSAnnotation.stringValue(name: String): String? {
        return arguments.firstOrNull { it.name?.asString() == name }?.value as? String
    }

    private fun KSAnnotation.booleanValue(name: String): Boolean? {
        return arguments.firstOrNull { it.name?.asString() == name }?.value as? Boolean
    }

    private fun KSAnnotation.intValue(name: String): Int? {
        return arguments.firstOrNull { it.name?.asString() == name }?.value as? Int
    }

    private inline fun <reified E : Enum<E>> KSAnnotation.enumValue(name: String, defaultValue: E): E {
        val value = arguments.firstOrNull { it.name?.asString() == name }?.value?.toString()
        return enumValues<E>().firstOrNull { it.name == value } ?: defaultValue
    }

    private fun KSAnnotation.lowQueryOperator(): LowQueryOperator {
        val qualifiedName = annotationType.resolve().declaration.qualifiedName?.asString()
        if (qualifiedName == LOW_QUERY_PARAM_ANNOTATION) {
            return enumValue("operator", LowQueryOperator.EQ)
        }
        return LOW_QUERY_OPERATOR_BY_ANNOTATION.getValue(checkNotNull(qualifiedName))
    }

    private fun String.defaultParameterName(operator: LowQueryOperator): String {
        if (operator != LowQueryOperator.IN && operator != LowQueryOperator.NOT_IN) {
            return this
        }
        if (endsWith("s")) {
            return this
        }
        return "${this}s"
    }

    private fun KSType.renderParamType(operator: LowQueryOperator, forceNullable: Boolean): String {
        val scalarType = renderType(forceNullable = false, preserveNullable = false)
        if (operator == LowQueryOperator.IN || operator == LowQueryOperator.NOT_IN) {
            val nullableSuffix = if (forceNullable) "?" else ""
            return "Collection<$scalarType>$nullableSuffix"
        }
        return renderType(forceNullable = forceNullable, preserveNullable = true)
    }

    private fun KSType.renderType(forceNullable: Boolean, preserveNullable: Boolean): String {
        val declaration = declaration
        val qualifiedName = declaration.qualifiedName?.asString()
        val baseName = when {
            qualifiedName == null -> toString().removeSuffix("?")
            declaration.packageName.asString() == "kotlin" -> declaration.simpleName.asString()
            else -> qualifiedName
        }
        val typeArguments = arguments
            .mapNotNull { it.type?.resolve()?.renderType(forceNullable = false, preserveNullable = true) }
            .takeIf { it.isNotEmpty() }
            ?.joinToString(prefix = "<", postfix = ">")
            ?: ""
        val nullableSuffix = if (forceNullable || preserveNullable && isMarkedNullable) "?" else ""
        return "$baseName$typeArguments$nullableSuffix"
    }

    private fun error(message: String, symbol: KSAnnotated) {
        hasErrors = true
        logger.error(message, symbol)
    }

    private companion object {
        private val LOW_QUERY_OPERATOR_BY_ANNOTATION = mapOf(
            "$ANNOTATION_PACKAGE.Eq" to LowQueryOperator.EQ,
            "$ANNOTATION_PACKAGE.Ne" to LowQueryOperator.NE,
            "$ANNOTATION_PACKAGE.Like" to LowQueryOperator.LIKE,
            "$ANNOTATION_PACKAGE.StartsWith" to LowQueryOperator.STARTS_WITH,
            "$ANNOTATION_PACKAGE.EndsWith" to LowQueryOperator.ENDS_WITH,
            "$ANNOTATION_PACKAGE.Gt" to LowQueryOperator.GT,
            "$ANNOTATION_PACKAGE.Ge" to LowQueryOperator.GE,
            "$ANNOTATION_PACKAGE.Lt" to LowQueryOperator.LT,
            "$ANNOTATION_PACKAGE.Le" to LowQueryOperator.LE,
            "$ANNOTATION_PACKAGE.In" to LowQueryOperator.IN,
            "$ANNOTATION_PACKAGE.NotIn" to LowQueryOperator.NOT_IN,
        )

        private val LOW_QUERY_FIELD_ANNOTATIONS =
            LOW_QUERY_OPERATOR_BY_ANNOTATION.keys + LOW_QUERY_PARAM_ANNOTATION

        private val LOW_QUERY_ORDER_ANNOTATIONS = setOf(
            ORDER_BY_ASC_ANNOTATION,
            ORDER_BY_DESC_ANNOTATION,
        )
    }
}
