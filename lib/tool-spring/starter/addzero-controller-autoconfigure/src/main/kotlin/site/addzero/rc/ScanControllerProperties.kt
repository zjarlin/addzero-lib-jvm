package site.addzero.site.addzero.rc

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "expression.scan.controller")
data class ScanControllerProperties(
    val pkg: String = "site.addzero",
    val expression: String = "execution(* ${pkg}..*Controller*+.*(..))"
)
