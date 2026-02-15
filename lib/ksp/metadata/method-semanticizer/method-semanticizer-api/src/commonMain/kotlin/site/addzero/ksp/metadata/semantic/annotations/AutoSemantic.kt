package site.addzero.ksp.metadata.semantic.annotations

/**
 * 标记在接口或类上，启用方法语义化扩展生成
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class AutoSemantic

/**
 * 手动定义单条语义化变体（注解方式）
 * 处理器会将其与 SPI 提供的元数据合并
 */
@Target(AnnotationTarget.FUNCTION)
@Repeatable
@Retention(AnnotationRetention.SOURCE)
annotation class SemanticVariation(
    val name: String,
    val args: Array<String> = [],
    val doc: String = ""
)