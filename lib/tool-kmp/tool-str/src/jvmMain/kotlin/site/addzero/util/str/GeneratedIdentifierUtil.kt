package site.addzero.util.str

import PinYin4JUtils

/**
 * 将任意描述文本转换为生成方法名。
 *
 * @param defaultName 默认名称。
 */
fun String.toGeneratedMethodName(
    defaultName: String = "generatedMethod",
): String {
    return identifierWords(defaultName).toLowerCamelIdentifier(defaultName)
}

/**
 * 将任意描述文本转换为生成属性名。
 *
 * @param defaultName 默认名称。
 */
fun String.toGeneratedPropertyName(
    defaultName: String = "fieldValue",
): String {
    return identifierWords(defaultName).toLowerCamelIdentifier(defaultName)
}

/**
 * 将任意描述文本转换为生成类型名。
 *
 * @param defaultName 默认名称。
 */
fun String.toGeneratedTypeName(
    defaultName: String = "GeneratedModel",
): String {
    return identifierWords(defaultName).toUpperCamelIdentifier(defaultName)
}

/**
 * 将文本拆成可用作标识符的单词序列。
 *
 * @param defaultName 默认名称。
 */
private fun String.identifierWords(
    defaultName: String,
): List<String> {
    val romanized = romanizeIdentifierSource(trim())
    val words = romanized.toIdentifierWords()
    if (words.isNotEmpty()) {
        return words
    }
    return defaultName.toIdentifierWords().ifEmpty {
        listOf(defaultName.lowercase())
    }
}

/**
 * 将源文本做 JVM 侧转写。
 */
private fun romanizeIdentifierSource(
    source: String,
): String {
    if (source.isBlank()) {
        return ""
    }
    val builder = StringBuilder()
    source.forEach { char ->
        when {
            char.isWhitespace() -> {
                builder.append(' ')
            }

            char.code <= 127 -> {
                builder.append(char)
            }

            else -> {
                val pinyin =
                    runCatching {
                        PinYin4JUtils.charToPinyin(char, false, null)
                    }.getOrNull()
                if (pinyin.isNullOrBlank() || pinyin == char.toString()) {
                    builder.append(char)
                } else {
                    builder.append(pinyin)
                    builder.append(' ')
                }
            }
        }
    }
    return builder.toString()
}

/**
 * 将字符串规整为标识符单词。
 */
private fun String.toIdentifierWords(): List<String> {
    return replace(Regex("([a-z0-9])([A-Z])"), "$1 $2")
        .replace(Regex("([A-Z]+)([A-Z][a-z])"), "$1 $2")
        .replace(Regex("[^A-Za-z0-9]+"), " ")
        .trim()
        .split(Regex("\\s+"))
        .filter { it.isNotBlank() }
        .map { it.lowercase() }
}

/**
 * 将单词列表转成小驼峰标识符。
 *
 * @param defaultName 默认名称。
 */
private fun List<String>.toLowerCamelIdentifier(
    defaultName: String,
): String {
    val candidate =
        if (isEmpty()) {
            defaultName
        } else {
            first() + drop(1).joinToString(separator = "") { word ->
                word.capitalizeAscii()
            }
        }
    return candidate.ensureIdentifierPrefix(defaultName)
}

/**
 * 将单词列表转成大驼峰标识符。
 *
 * @param defaultName 默认名称。
 */
private fun List<String>.toUpperCamelIdentifier(
    defaultName: String,
): String {
    val candidate =
        if (isEmpty()) {
            defaultName
        } else {
            joinToString(separator = "") { word ->
                word.capitalizeAscii()
            }
        }
    return candidate.ensureIdentifierPrefix(defaultName)
}

/**
 * 将 ASCII 首字母改成大写。
 */
private fun String.capitalizeAscii(): String {
    return replaceFirstChar { char ->
        if (char.isLowerCase()) {
            char.titlecase()
        } else {
            char.toString()
        }
    }
}

/**
 * 保证标识符前缀合法。
 *
 * @param defaultName 默认名称。
 */
private fun String.ensureIdentifierPrefix(
    defaultName: String,
): String {
    val candidate =
        if (isBlank()) {
            defaultName
        } else {
            this
        }
    return if (candidate.first().isDigit()) {
        "_$candidate"
    } else {
        candidate
    }
}
