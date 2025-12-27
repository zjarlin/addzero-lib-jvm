package site.addzero.ioc.annotation

import kotlin.annotation.AnnotationTarget.CLASS

@Target(CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class ComponentScan(
    val packages: Array<String> = [],
    val defaultNamespace: String = "site.addzero.ioc.metadata"
)
