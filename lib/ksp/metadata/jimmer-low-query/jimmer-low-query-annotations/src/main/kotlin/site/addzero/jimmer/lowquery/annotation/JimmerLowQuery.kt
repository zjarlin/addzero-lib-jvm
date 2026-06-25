package site.addzero.jimmer.lowquery.annotation

/**
 * 配置一个 Jimmer 实体生成的低代码查询扩展函数。
 *
 * 实体字段上出现 @Eq、@Like、@In 等注解时也会自动参与生成；本注解负责覆盖函数名、可见性和 fetcher 策略。
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class JimmerLowQuery(
    /**
     * 生成的 KMutableRootQuery.ForEntity<E> 扩展函数名。
     */
    val functionName: String = "query",
    /**
     * 生成函数的可见性。
     */
    val visibility: JimmerLowQueryVisibility = JimmerLowQueryVisibility.PUBLIC,
    /**
     * select 阶段使用的 fetcher 策略。
     */
    val fetcher: JimmerLowQueryFetcher = JimmerLowQueryFetcher.ALL_SCALAR_FIELDS,
    /**
     * 生成的 KSqlClient 扩展函数名，用于通过实体对象创建查询。
     */
    val clientFunctionName: String = "createLowQuery",
    /**
     * 生成的 KSqlClient 入口函数可见性。
     */
    val clientVisibility: JimmerLowQueryVisibility = JimmerLowQueryVisibility.PUBLIC,
)

/**
 * 标记实体字段需要参与低代码查询函数入参和 where 条件生成。
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
annotation class JimmerLowQueryParam(
    /**
     * 生成函数的参数名，留空时使用实体字段名。
     */
    val name: String = "",
    /**
     * 当前参数对应的查询操作符。
     */
    val operator: JimmerLowQueryOperator = JimmerLowQueryOperator.EQ,
    /**
     * true 时参数生成可空类型，并在参数为空时跳过该 where 条件。
     */
    val nullable: Boolean = false,
)

/**
 * 字段等值查询，生成 `table.xxx eq param`。
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
annotation class Eq(
    /**
     * 生成函数的参数名，留空时使用实体字段名。
     */
    val name: String = "",
    /**
     * true 时参数生成可空类型，并在参数为空时跳过该 where 条件。
     */
    val nullable: Boolean = false,
)

/**
 * 字段不等查询，生成 `table.xxx ne param`。
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
annotation class Ne(
    /**
     * 生成函数的参数名，留空时使用实体字段名。
     */
    val name: String = "",
    /**
     * true 时参数生成可空类型，并在参数为空时跳过该 where 条件。
     */
    val nullable: Boolean = false,
)

/**
 * 字段模糊查询，默认生成 `table.xxx ilike(param, LikeMode.ANYWHERE)`。
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
annotation class Like(
    /**
     * 生成函数的参数名，留空时使用实体字段名。
     */
    val name: String = "",
    /**
     * true 时参数生成可空类型，并在参数为空时跳过该 where 条件。
     */
    val nullable: Boolean = false,
)

/**
 * 字段前缀匹配查询，生成 `table.xxx like(param, LikeMode.START)`。
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
annotation class StartsWith(
    /**
     * 生成函数的参数名，留空时使用实体字段名。
     */
    val name: String = "",
    /**
     * true 时参数生成可空类型，并在参数为空时跳过该 where 条件。
     */
    val nullable: Boolean = false,
)

/**
 * 字段后缀匹配查询，生成 `table.xxx like(param, LikeMode.END)`。
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
annotation class EndsWith(
    /**
     * 生成函数的参数名，留空时使用实体字段名。
     */
    val name: String = "",
    /**
     * true 时参数生成可空类型，并在参数为空时跳过该 where 条件。
     */
    val nullable: Boolean = false,
)

/**
 * 字段大于查询，生成 `table.xxx gt param`。
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
annotation class Gt(
    /**
     * 生成函数的参数名，留空时使用实体字段名。
     */
    val name: String = "",
    /**
     * true 时参数生成可空类型，并在参数为空时跳过该 where 条件。
     */
    val nullable: Boolean = false,
)

/**
 * 字段大于等于查询，生成 `table.xxx ge param`。
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
annotation class Ge(
    /**
     * 生成函数的参数名，留空时使用实体字段名。
     */
    val name: String = "",
    /**
     * true 时参数生成可空类型，并在参数为空时跳过该 where 条件。
     */
    val nullable: Boolean = false,
)

/**
 * 字段小于查询，生成 `table.xxx lt param`。
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
annotation class Lt(
    /**
     * 生成函数的参数名，留空时使用实体字段名。
     */
    val name: String = "",
    /**
     * true 时参数生成可空类型，并在参数为空时跳过该 where 条件。
     */
    val nullable: Boolean = false,
)

/**
 * 字段小于等于查询，生成 `table.xxx le param`。
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
annotation class Le(
    /**
     * 生成函数的参数名，留空时使用实体字段名。
     */
    val name: String = "",
    /**
     * true 时参数生成可空类型，并在参数为空时跳过该 where 条件。
     */
    val nullable: Boolean = false,
)

/**
 * 字段集合包含查询，生成 `table.xxx valueIn params`。
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
annotation class In(
    /**
     * 生成函数的参数名，留空时使用实体字段名的简单复数形式。
     */
    val name: String = "",
    /**
     * true 时集合参数本身生成可空类型，并在参数为空时跳过该 where 条件。
     */
    val nullable: Boolean = false,
)

/**
 * 字段集合排除查询，生成 `table.xxx valueNotIn params`。
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
annotation class NotIn(
    /**
     * 生成函数的参数名，留空时使用实体字段名的简单复数形式。
     */
    val name: String = "",
    /**
     * true 时集合参数本身生成可空类型，并在参数为空时跳过该 where 条件。
     */
    val nullable: Boolean = false,
)

/**
 * 字段升序排序，priority 越小越靠前。
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
annotation class OrderByAsc(
    /**
     * 排序优先级，数值越小越先排序。
     */
    val priority: Int = 0,
)

/**
 * 字段降序排序，priority 越小越靠前。
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
annotation class OrderByDesc(
    /**
     * 排序优先级，数值越小越先排序。
     */
    val priority: Int = 0,
)

/**
 * 低代码查询 where 操作符。
 */
enum class JimmerLowQueryOperator {
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

/**
 * 低代码查询 select 阶段 fetcher 策略。
 */
enum class JimmerLowQueryFetcher {
    ALL_SCALAR_FIELDS,
    ALL_TABLE_FIELDS,
    TABLE,
}

/**
 * 低代码查询生成函数可见性。
 */
enum class JimmerLowQueryVisibility {
    PUBLIC,
    INTERNAL,
    PRIVATE,
}
