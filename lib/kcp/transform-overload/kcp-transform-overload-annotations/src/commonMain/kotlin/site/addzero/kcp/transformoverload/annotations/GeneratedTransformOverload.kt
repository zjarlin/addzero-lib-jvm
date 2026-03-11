package site.addzero.kcp.transformoverload.annotations

/**
 * 内部使用的标记注解，用于标记由插件生成的转换重载方法。
 * 此注解不应该在源代码中手动使用。
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.BINARY)
annotation class GeneratedTransformOverload
