package site.addzero.rc

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "site.addzero.scan.dict.trans")
class AddzeroDictTransProperties(
    var pkg: String = "site.addzero",
    // 优化默认表达式，使用更精确的controller包扫描
    var expression: String = "execution(* ${pkg}..controller.*Controller.*(..))"
)

