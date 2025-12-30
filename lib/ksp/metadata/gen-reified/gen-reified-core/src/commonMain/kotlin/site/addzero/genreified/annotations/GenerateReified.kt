package site.addzero.kcp.annotations

/**
 * 标记一个类或接口，为其生成 reified 类型参数的方法
 *
 * 插件会扫描标记类中所有包含 KClass 参数的方法，并生成对应的 inline reified 版本
 *
 * 规则：
 * - 自动识别所有 KClass 或 Class 类型的参数
 * - 当有多个 KClass 参数时，生成多个 reified 泛型参数
 * - 生成的方法会移除这些参数，并添加对应的 reified 类型参数
 *
 * 示例：
 * ```kotlin
 * @GenerateReified
 * interface ViewContainer {
 *     fun findById(id: ID, clazz: KClass<*>): Any?
 *     // 生成: inline fun <reified T : Any> findById(id: ID): T?
 *
 *     fun convert(source: Any, from: KClass<*>, to: KClass<*>): Any?
 *     // 生成: inline fun <reified F : Any, reified T : Any> convert(source: Any): T?
 * }
 * ```
 *
 * @param value 生成的方法名称
 *              - 空字符串 (默认): 保留原始方法名
 *              - 指定名称: 使用指定的名称
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FILE, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class GenerateReified(
    val value: String = ""
)
