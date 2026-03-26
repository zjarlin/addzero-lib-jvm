package site.addzero.annotation

/**
 * 标记需要从状态管理中排除的参数
 * 使用此注解的参数不会包含在生成的状态类中
 *
 * 使用示例：
 * ```kotlin
 * @Composable
 * @ComposeAssist
 * fun MyComponent(
 *     data: List<String>,
 *     @AssistExclude modifier: Modifier = Modifier,
 *     @AssistExclude key: String = "default"
 * ) {
 *     // 实现
 * }
 * ```
 *
 * 在上面的例子中，`modifier` 和 `key` 参数不会包含在生成的状态类中
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.SOURCE)
annotation class AssistExclude
