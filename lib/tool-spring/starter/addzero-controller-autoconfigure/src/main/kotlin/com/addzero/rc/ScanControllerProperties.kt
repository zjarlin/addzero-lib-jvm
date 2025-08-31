package io.gitee.zjarlin.com.addzero.rc

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "expression.scan.controller")
data class ScanControllerProperties(
    val pkg: String = "com.addzero",
    val expression: String = "execution(* ${pkg}..*Controller*+.*(..))"
)
