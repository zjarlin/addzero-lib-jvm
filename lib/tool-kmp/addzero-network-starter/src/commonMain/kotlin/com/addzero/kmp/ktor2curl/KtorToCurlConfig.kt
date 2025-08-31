package com.addzero.kmp.ktor2curl

class KtorToCurlConfig {
    var converter = object : CurlLogger {
        override fun log(curl: String) = Unit
    }
    var excludedHeaders: Set<String> = emptySet()
    var maskedHeaders: Set<String> = emptySet()
}
