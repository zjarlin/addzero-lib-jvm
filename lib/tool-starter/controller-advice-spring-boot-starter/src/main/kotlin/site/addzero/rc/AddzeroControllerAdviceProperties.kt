package site.addzero.rc

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@ConfigurationProperties(prefix = "site.addzero.scan.controller.advice")
@Component
class AddzeroControllerAdviceProperties(
    var includePackages: List<String> = listOf("site.addzero"),
    var expression: String = "execution(* site.addzero..*Controller*+.*(..))",
    var excludePackages: List<String> = emptyList()
)
