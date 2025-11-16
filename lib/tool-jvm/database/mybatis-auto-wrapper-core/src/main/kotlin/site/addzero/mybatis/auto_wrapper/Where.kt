package site.addzero.mybatis.auto_wrapper


/**
 *
 *
 * @author zjarlin
 */
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
@JvmRepeatable(Wheres::class)
annotation class Where(
    /**
     * 查询操作符，如 =, !=, >, >=, <, <=, like, in, null, notNull 等
     */
    val value: String = "=",
    /**
     * 数据库列名，默认为空则使用字段名
     */
    val column: String = "",
    /**
     * 是否为 join 条件
     */
    val join: Boolean = false,
    /**
     * 是否忽略该 Where 条件
     */
    val ignore: Boolean = false,
    /**
     * SpEL 表达式，用于动态判断是否应用此条件
     * 表达式中可以使用以下变量：
     * - #value: 当前字段的值
     * - #field: 当前字段对象
     * - #dto: 整个 DTO 对象
     *
     * 示例：
     * - condition = "#value != null" (当值不为null时应用)
     * - condition = "#value != null && #value != ''" (当值不为null且不为空字符串时应用)
     * - condition = "#value == null" (当值为null时应用，适用于 IS NULL 查询)
     * - condition = "#dto.status == 'active' && #value != null" (复杂条件)
     *
     * 默认为空字符串，表示使用默认的条件判断逻辑
     */
    val condition: String = ""
)
