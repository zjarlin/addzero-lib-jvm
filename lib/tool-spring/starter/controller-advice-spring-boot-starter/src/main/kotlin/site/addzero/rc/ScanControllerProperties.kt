package site.addzero.rc

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@ConfigurationProperties(prefix = "expression.scan.controller")
@Component
data class ScanControllerProperties(
    val pkg: String = "site.addzero",
    val expression: String = "execution(* ${pkg}..*Controller*+.*(..))"
)
