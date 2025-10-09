package site.addzero.rc

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Primary

@Primary
@ConfigurationProperties(prefix = "site.addzero.scan.dict.trans")
open class AddzeroDictTransProperties(
    var pkg: String = "site.addzero",
    var expression: String = "execution(* ${pkg}..*Controller*+.*(..))"
)
