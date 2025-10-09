package site.addzero.rc

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Primary

@ConfigurationProperties(prefix = "site.addzero.scan.dict.trans")
@Primary
data class DictTransProperties(
    var pkg: String = "site.addzero",
    var expression: String = "execution(* ${pkg}..*Controller*+.*(..))"
)
