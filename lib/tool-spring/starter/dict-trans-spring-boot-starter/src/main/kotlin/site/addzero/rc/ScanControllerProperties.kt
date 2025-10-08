package site.addzero.rc

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Component

@ConfigurationProperties(prefix = "expression.scan.controller")
@Primary
data class ScanControllerProperties(
    var pkg: String = "site.addzero",
    var expression: String = "execution(* ${pkg}..*Controller*+.*(..))"
)
