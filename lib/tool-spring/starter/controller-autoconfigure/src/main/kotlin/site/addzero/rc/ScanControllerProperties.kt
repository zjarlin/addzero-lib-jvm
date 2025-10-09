package site.addzero.rc

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@ConfigurationProperties(prefix = "expression.scan.controller")
data class ScanControllerProperties(
    var pkg: String = "site.addzero",
    var expression: String = "execution(* ${'$'}{pkg}..*Controller*+.*(..))",
    var excludePackages: List<String> = emptyList()
)