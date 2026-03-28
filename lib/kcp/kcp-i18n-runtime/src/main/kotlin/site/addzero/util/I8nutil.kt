package site.addzero.util

import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import java.util.Locale
import java.util.Properties
import java.util.concurrent.ConcurrentHashMap

object I8nutil {
    private val bundles = ConcurrentHashMap<String, Properties>()
    @Volatile
    private var currentLocaleOverride: String? = null

    fun t(
        key: String,
        fallback: String,
        basePath: String,
    ): String {
        val normalizedBasePath = basePath
            .trim()
            .trim('/')
            .ifBlank { "i18n" }
        val resolvedValue = buildLocaleCandidates(resolveLocale())
            .asSequence()
            .mapNotNull { locale -> loadValue(normalizedBasePath, locale, key) }
            .firstOrNull { it.isNotBlank() }
        return resolvedValue ?: fallback
    }

    fun setLocale(locale: String) {
        currentLocaleOverride = normalizeLocale(locale)
    }

    fun clearLocale() {
        currentLocaleOverride = null
    }

    fun currentLocale(): String {
        return resolveLocale()
    }

    private fun resolveLocale(): String {
        return currentLocaleOverride ?: normalizeLocale(
            Locale.getDefault().toLanguageTag().ifBlank { Locale.getDefault().language },
        )
    }

    private fun loadValue(
        basePath: String,
        locale: String,
        key: String,
    ): String? {
        val resourceNames = buildResourceNames(basePath, locale)
        return resourceNames
            .asSequence()
            .map { resourceName -> bundles.computeIfAbsent(resourceName, ::loadProperties) }
            .mapNotNull { bundle -> bundle.getProperty(key) }
            .firstOrNull()
    }

    private fun buildLocaleCandidates(locale: String): List<String> {
        val normalizedLocale = normalizeLocale(locale)
        val languageOnly = normalizedLocale.substringBefore('-')
        return buildList {
            add(normalizedLocale)
            if (languageOnly.isNotBlank() && languageOnly != normalizedLocale) {
                add(languageOnly)
            }
        }
    }

    private fun buildResourceNames(
        basePath: String,
        locale: String,
    ): List<String> {
        val hyphenLocale = normalizeLocale(locale)
        val underscoreLocale = hyphenLocale.replace('-', '_')
        return buildList {
            add("$basePath/$hyphenLocale.properties")
            if (underscoreLocale != hyphenLocale) {
                add("$basePath/$underscoreLocale.properties")
            }
        }
    }

    private fun normalizeLocale(locale: String): String {
        val trimmed = locale.trim().ifBlank { "zh" }
        return trimmed.replace('_', '-')
    }

    private fun loadProperties(resourceName: String): Properties {
        val properties = Properties()
        val classLoader = Thread.currentThread().contextClassLoader ?: javaClass.classLoader
        val resourceStream = classLoader.getResourceAsStream(resourceName)
            ?: javaClass.classLoader.getResourceAsStream(resourceName)
            ?: return properties
        resourceStream.use { stream ->
            InputStreamReader(stream, StandardCharsets.UTF_8).use { reader ->
                parseProperties(reader).forEach { (key, value) ->
                    properties.setProperty(key, value)
                }
            }
        }
        return properties
    }

    private fun parseProperties(reader: InputStreamReader): Map<String, String> {
        val entries = linkedMapOf<String, String>()
        reader.readLines().forEach { rawLine ->
            val trimmedStart = rawLine.trimStart()
            if (trimmedStart.isBlank() || trimmedStart.startsWith("#") || trimmedStart.startsWith("!")) {
                return@forEach
            }
            val separatorIndex = findSeparatorIndex(rawLine)
            val rawKey = if (separatorIndex >= 0) {
                rawLine.substring(0, separatorIndex)
            } else {
                rawLine
            }
            val rawValue = if (separatorIndex >= 0) {
                rawLine.substring(separatorIndex + 1).trimStart()
            } else {
                ""
            }
            entries[decodeEscapes(rawKey)] = decodeEscapes(rawValue)
        }
        return entries
    }

    private fun findSeparatorIndex(line: String): Int {
        for (index in line.indices) {
            val char = line[index]
            if ((char == '=' || char == ':') && !isEscaped(line, index)) {
                return index
            }
        }
        return -1
    }

    private fun isEscaped(line: String, index: Int): Boolean {
        var backslashCount = 0
        var cursor = index - 1
        while (cursor >= 0 && line[cursor] == '\\') {
            backslashCount += 1
            cursor -= 1
        }
        return backslashCount % 2 == 1
    }

    private fun decodeEscapes(text: String): String {
        if ('\\' !in text) {
            return text
        }
        val decoded = StringBuilder(text.length)
        var index = 0
        while (index < text.length) {
            val current = text[index]
            if (current != '\\' || index == text.lastIndex) {
                decoded.append(current)
                index += 1
                continue
            }
            val escaped = text[index + 1]
            when (escaped) {
                't' -> decoded.append('\t')
                'r' -> decoded.append('\r')
                'n' -> decoded.append('\n')
                'f' -> decoded.append('\u000C')
                'u' -> {
                    val unicodeEnd = index + 6
                    if (unicodeEnd <= text.length) {
                        val unicodeValue = text.substring(index + 2, unicodeEnd).toInt(16)
                        decoded.append(unicodeValue.toChar())
                        index += 6
                        continue
                    }
                    decoded.append(escaped)
                }
                else -> decoded.append(escaped)
            }
            index += 2
        }
        return decoded.toString()
    }
}

fun i18nT(
    key: String,
    fallback: String,
    basePath: String,
): String {
    return I8nutil.t(key, fallback, basePath)
}
