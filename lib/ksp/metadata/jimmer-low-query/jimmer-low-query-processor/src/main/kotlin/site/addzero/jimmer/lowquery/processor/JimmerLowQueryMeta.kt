package site.addzero.jimmer.lowquery.processor

internal enum class LowQueryOperator {
    EQ,
    NE,
    LIKE,
    STARTS_WITH,
    ENDS_WITH,
    GT,
    GE,
    LT,
    LE,
    IN,
    NOT_IN,
}

internal enum class LowQueryFetcher {
    ALL_SCALAR_FIELDS,
    ALL_TABLE_FIELDS,
    TABLE,
}

internal enum class LowQueryVisibility(
    val code: String,
) {
    PUBLIC("public"),
    INTERNAL("internal"),
    PRIVATE("private"),
}

internal enum class LowQueryOrderDirection {
    ASC,
    DESC,
}

internal data class LowQueryParamMeta(
    val propertyName: String,
    val parameterName: String,
    val typeName: String,
    val operator: LowQueryOperator,
    val nullable: Boolean,
)

internal data class LowQueryOrderMeta(
    val propertyName: String,
    val direction: LowQueryOrderDirection,
    val priority: Int,
)

internal data class LowQueryEntityMeta(
    val packageName: String,
    val simpleName: String,
    val qualifiedName: String,
    val functionName: String,
    val clientFunctionName: String,
    val visibility: LowQueryVisibility,
    val clientVisibility: LowQueryVisibility,
    val fetcher: LowQueryFetcher,
    val params: List<LowQueryParamMeta>,
    val orders: List<LowQueryOrderMeta>,
)
