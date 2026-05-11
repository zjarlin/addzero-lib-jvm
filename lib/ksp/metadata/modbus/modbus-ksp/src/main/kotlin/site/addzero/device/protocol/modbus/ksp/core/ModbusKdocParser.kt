package site.addzero.device.protocol.modbus.ksp.core

/**
 * KDoc 解析器。
 */
object ModbusKdocParser {
    fun parse(rawDoc: String?, fallbackSummary: String): ModbusDocModel {
        val doc = rawDoc?.trim().orEmpty()
        if (doc.isBlank()) {
            return ModbusDocModel(summary = fallbackSummary)
        }

        val normalizedLines =
            doc.lineSequence()
                .map { line ->
                    line.trim()
                        .removePrefix("*")
                        .trim()
                }
                .toList()

        val narrativeLines = mutableListOf<String>()
        val parameterDocs = linkedMapOf<String, StringBuilder>()
        var currentParam: String? = null

        normalizedLines.forEach { line ->
            when {
                line.isBlank() -> {
                    currentParam = null
                    if (narrativeLines.isNotEmpty() && narrativeLines.last().isNotEmpty()) {
                        narrativeLines += ""
                    }
                }

                line.startsWith("@param ") -> {
                    val match = PARAM_REGEX.find(line)
                    if (match != null) {
                        val name = match.groupValues[1]
                        val text = match.groupValues[2].trim()
                        parameterDocs.getOrPut(name) { StringBuilder() }.append(text)
                        currentParam = name
                    }
                }

                line.startsWith("@") -> {
                    currentParam = null
                }

                currentParam != null -> {
                    val builder = parameterDocs.getValue(currentParam)
                    if (builder.isNotEmpty()) {
                        builder.append(' ')
                    }
                    builder.append(line)
                }

                else -> narrativeLines += line
            }
        }

        val compactNarrative =
            narrativeLines
                .fold(mutableListOf<String>()) { acc, line ->
                    if (line.isBlank()) {
                        if (acc.isNotEmpty() && acc.last().isNotEmpty()) {
                            acc += ""
                        }
                    } else {
                        acc += line
                    }
                    acc
                }
                .dropLastWhile(String::isEmpty)

        val summary = compactNarrative.firstOrNull(String::isNotBlank) ?: fallbackSummary
        val descriptionLines =
            compactNarrative
                .dropWhile(String::isBlank)
                .drop(1)
                .dropWhile(String::isBlank)

        return ModbusDocModel(
            summary = summary,
            descriptionLines = descriptionLines,
            parameterDocs = parameterDocs.mapValues { (_, value) -> value.toString().trim() }.filterValues(String::isNotBlank),
        )
    }

    private val PARAM_REGEX = Regex("""^@param\s+([A-Za-z0-9_]+)\s+(.*)$""")
}
