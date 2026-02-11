package site.addzero.ioc.strategy

/**
 * Metadata collected from @Bean annotated elements
 */
data class BeanInfo(
    val name: String,
    val initType: InitType,
    val order: Int = 0
)
