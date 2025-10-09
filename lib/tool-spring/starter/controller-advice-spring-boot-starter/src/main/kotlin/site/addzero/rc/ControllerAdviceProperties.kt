package site.addzero.rc

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "site.addzero.scan.controller.advice")
data class ControllerAdviceProperties(
    var includePackages: List<String> = listOf("site.addzero"),
    var expression: String = "execution(* site.addzero..*Controller*+.*(..))",
    var excludePackages: List<String> = emptyList()
)
