package site.addzero.ioc.strategy

/**
 * Metadata collected from @Bean annotated elements
 */
data class BeanInfo(
    /** fully qualified name (or receiver::fqn for extensions) */
    val name: String,
    val initType: InitType,
    val order: Int = Int.MAX_VALUE,
    /** explicit bean name from @Bean(name=...), empty means auto-derive */
    val beanName: String = "",
    /** tags from @Bean(tags=...) for grouping */
    val tags: List<String> = emptyList(),
    /** whether this bean participates in registration/execution */
    val enabled: Boolean = true,
    /** bean dependencies, resolved by beanName / qualifiedName / simpleName */
    val dependsOn: List<String> = emptyList()
) {
    val simpleName: String
        get() = name.substringAfterLast(".")

    val qualifiedName: String
        get() = name.substringAfter("::")

    val resolvedBeanName: String
        get() = beanName.ifEmpty { simpleName.replaceFirstChar { it.lowercase() } }
}
