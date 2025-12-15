package site.addzero.rc

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@ConfigurationProperties(prefix = "site.addzero.scan.dict.trans")
class AddzeroDictTransProperties(
    var pkg: String = "site.addzero",
    var expression: String = "execution(* ${pkg}..*Controller*+.*(..))"
)

