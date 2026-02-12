package site.addzero.ioc.strategy

/**
 * Metadata collected from @Bean annotated elements
 */
data class BeanInfo(
    /** fully qualified name (or receiver::fqn for extensions) */
    val name: String,
    val initType: InitType,
    val order: Int = 0,
    /** explicit bean name from @Bean(name=...), empty means auto-derive */
    val beanName: String = "",
    /** tags from @Bean(tags=...) for grouping */
    val tags: List<String> = emptyList()
)
