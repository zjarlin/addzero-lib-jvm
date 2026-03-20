package site.addzero.util

import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import java.util.Locale
import java.util.Properties
import java.util.concurrent.ConcurrentHashMap

object I8nutil {
    private val bundles = ConcurrentHashMap<String, Properties>()

    fun t(
        key: String,
        locale: String,
        basePath: String,
    ): String {
        val normalizedLocale = locale.ifBlank {
            Locale.getDefault().language.ifBlank { "en" }
        }
        val normalizedBasePath = basePath
            .trim()
            .trim('/')
            .ifBlank { "i18n" }
        val resourceName = "$normalizedBasePath/$normalizedLocale.properties"
        val bundle = bundles.computeIfAbsent(resourceName, ::loadProperties)
        return bundle.getProperty(key) ?: key
    }

    private fun loadProperties(resourceName: String): Properties {
        val properties = Properties()
        val classLoader = Thread.currentThread().contextClassLoader ?: javaClass.classLoader
        val resourceStream = classLoader.getResourceAsStream(resourceName)
            ?: javaClass.classLoader.getResourceAsStream(resourceName)
            ?: return properties
        resourceStream.use { stream ->
            InputStreamReader(stream, StandardCharsets.UTF_8).use(properties::load)
        }
        return properties
    }
}

fun i18nT(
    key: String,
    locale: String,
    basePath: String,
): String {
    return I8nutil.t(key, locale, basePath)
}
