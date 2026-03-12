package site.addzero.network.call.payment.internal

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.text.Charsets.UTF_8

internal object PaymentConfigValue {

    fun required(name: String): String {
        val value = optional(name)
        require(!value.isNullOrBlank()) {
            "Missing required payment config: $name"
        }
        return value
    }

    fun optional(name: String, defaultValue: String? = null): String? {
        val environmentValue = System.getenv(name)?.trim()
        if (!environmentValue.isNullOrEmpty()) {
            return environmentValue
        }

        val systemPropertyValue = System.getProperty(name)?.trim()
        if (!systemPropertyValue.isNullOrEmpty()) {
            return systemPropertyValue
        }

        return defaultValue
    }

    fun readContent(rawValue: String): String {
        val normalizedValue = rawValue.trim()
        val path = Paths.get(normalizedValue)
        if (Files.exists(path)) {
            return String(Files.readAllBytes(path), UTF_8).trim()
        }
        return normalizedValue
    }

    fun pathIfExists(rawValue: String): Path? {
        val normalizedValue = rawValue.trim()
        val path = Paths.get(normalizedValue)
        if (Files.exists(path)) {
            return path
        }
        return null
    }
}
