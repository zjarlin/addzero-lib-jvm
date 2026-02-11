package site.addzero.ioc.annotation

import kotlin.annotation.AnnotationTarget.CLASS

@Target(CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class ComponentScan(
    /** 要扫描的包路径，为空时自动使用声明类所在的包 */
    val packages: Array<String> = [],
    /** 要排除的包路径 */
    val excludePackages: Array<String> = [],
    /** 默认命名空间（仅当 packages 为空且无法获取声明类包名时使用） */
    val defaultNamespace: String = "site.addzero.ioc.metadata"
)
