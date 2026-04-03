package site.addzero.kcp.spreadpack

/**
 * 内部使用的标记注解，用于标记由插件生成的参数包展开重载方法。
 * 此注解不应该在源代码中手动使用。
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.BINARY)
annotation class GeneratedSpreadPackOverload
