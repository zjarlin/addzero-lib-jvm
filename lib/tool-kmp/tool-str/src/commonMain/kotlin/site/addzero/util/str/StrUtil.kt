package site.addzero.util.str

import kotlin.text.isNullOrEmpty


/**
 * 清理字符串中的空白字符
 * 包括：
 * 1. 移除首尾空白字符
 * 2. 将连续的空白字符替换为单个空格
 * 3. 移除不可见字符
 *
 * @param str 要清理的字符串
 * @return 清理后的字符串
 */
fun String?.cleanBlank(): String {
    if (this.isNullOrEmpty()) return ""

    return this!!.trim().replace(Regex("\\s+"), " ") // 将连续的空白字符替换为单个空格
        .filter { it.isDefined() } // 移除不可见字符
}

/**
 * 判断字符是否可见
 */
private fun Char.isDefined(): Boolean {
    return this.code in 32..126 || this.code in 0x4E00..0x9FFF
}


fun <T> groupBySeparator(lines: List<T>, predicate: (T) -> Boolean): Map<T, List<T>> {
    val separatorIndices = lines.indices.filter { predicate(lines[it]) }
    return separatorIndices.mapIndexed { index, spe ->
        val next = if (index + 1 < separatorIndices.size) {
            separatorIndices[index + 1]
        } else {
            lines.size // 如果没有下一个分隔符，取行的总数
        }

        val subList = lines.subList(spe + 1, next)
        lines[spe] to subList // 使用 Pair 进行配对
    }.toMap()
}


fun String?.makeSurroundWith(fix: String): String {
    val addPrefixIfNot = this?.addPrefixIfNot(fix) ?: fix
    val addSuffixIfNot = addPrefixIfNot.addSuffixIfNot(fix)
    return addSuffixIfNot
}


/**
 * 扩展函数：用HTML P标签包裹
 */
fun String?.makeSurroundWithHtmlP(): String {
    if (this.isNullOrBlank()) {
        return ""
    }
    return this.addPrefixIfNot("<p>").addSuffixIfNot("</p>")
}


fun String?.removeNotChinese(): String {
    if (this.isNullOrBlank()) {
        return ""
    }
    val regex = "[^\u4E00-\u9FA5]"
    val s1 = this.replace(regex.toRegex(), "")
    return s1
}


/**
 * 如果字符串不以指定后缀结尾，则添加该后缀
 *
 * @param suffix 要添加的后缀
 * @param ignoreCase 是否忽略大小写，默认为false
 * @return 添加后缀后的字符串
 */
fun String?.addSuffixIfNot(suffix: String, ignoreCase: Boolean = false): String {
    if (this == null) {
        return suffix
    }

    return if (this.endsWith(suffix, ignoreCase)) {
        this
    } else {
        this + suffix
    }
}


fun CharSequence?.isNotBlank(): Boolean {
    return !this.isBlank()
}


/**
 * 扩展函数：提取路径部分
 */
fun String.getPathFromRight(n: Int): String {
    val parts = this.split(".").filter { it.isNotEmpty() }

    if (parts!!.size < n) {
        return this // 输入字符串中的路径部分不足n个，返回整个输入字符串
    }

    return parts.dropLast(n).joinToString(".")
}


fun String?.lowerCase(): String {
    if (this.isNullOrEmpty()) {
        return ""
    }
    return this!!.lowercase()
}

/**
 * 如果字符串不以指定前缀开头，则添加该前缀
 *
 * @param prefix 要添加的前缀
 * @param ignoreCase 是否忽略大小写，默认为false
 * @return 添加前缀后的字符串
 */
fun String?.addPrefixIfNot(prefix: String, ignoreCase: Boolean = false): String {
    if (this.isNullOrEmpty()) return ""
    return if (this.startsWith(prefix, ignoreCase)) this else prefix + this
}


/**
 * 字符串首字母小写
 */
fun String.lowerFirst(): String {
    if (isEmpty()) return this
    return first().lowercase() + substring(1)
}


/**
 * 检查字符串是否不在列表中（不区分大小写）
 */
infix fun String.ignoreCaseNotIn(collection: Collection<String>): Boolean {
    val b = this ignoreCaseIn collection
    return !b
}

/**
 * 检查字符串是否在列表中（不区分大小写）
 */
infix fun String.ignoreCaseIn(collection: Collection<String>): Boolean =
    collection.any { it.equals(this, ignoreCase = true) }

/**
 * 检查字符串是否包含任意一个给定的子字符串（忽略大小写）
 * @param substrings 要检查的子字符串集合
 * @return 如果包含任意一个子字符串则返回true，否则返回false
 */
fun String.containsAnyIgnoreCase(vararg substrings: String): Boolean {
    if (substrings.isEmpty()) return false
    val lowerThis = this.lowercase()
    return substrings.any { it.lowercase() in lowerThis }
}

fun String.toBigCamelCase(): String {
    return this.split("_").joinToString("") {
        it.replaceFirstChar { it.uppercase() }
    }
}

/**
 * 将列名转换为驼峰命名
 */
fun String.toLowCamelCase(): String {
    return this.split("_").joinToString("") {
        it.replaceFirstChar { c -> c.uppercase() }
    }.replaceFirstChar { it -> it.lowercase() }
}

infix fun String.ignoreCaseLike(other: String): Boolean {
    return this.contains(other, ignoreCase = true)
}

/**
 * 扩展函数：检查是否包含中文
 */
fun CharSequence?.containsChinese(): Boolean {
    if (this.isNullOrEmpty()) {
        return false
    }
    val containsMatchIn = Regex("[\\u4e00-\\u9fa5]").containsMatchIn(this!!)
    return containsMatchIn
}

fun join(split: String, vararg testStrs: CharSequence): String {
    if (testStrs.isEmpty()) {
        return ""
    }
    return testStrs.joinToString(split)
}

fun String.withPkg(pkg: String): String {
    return "$this/${pkg.replace(".", "/")}"
}

fun String.withFileName(fileName: String): String {
    return "$this/$fileName"
}

fun String.withFileSuffix(suffix: String = ".kt"): String {
    return "$this$suffix"
}


/**
 * 从字符序列中移除所有指定的字符串片段
 *
 * @param stringsToRemove 要移除的字符串片段
 * @return 移除指定字符串片段后的新字符串
 */
fun CharSequence?.removeAny(vararg stringsToRemove: String): String {
    if (this == null) return ""

    var result = this.toString()
    for (toRemove in stringsToRemove) {
        result = result.replace(toRemove, "")
    }
    return result
}


fun CharSequence.removeAnyQuote(): String {
    if (this.isBlank()) {
        return ""
    }
    return this.removeAny("\"", "\\")
}


/**
 * 删除空格或者引号
 * @param [testStrs]
 * @return [String]
 */
fun CharSequence.removeBlankOrQuotation(): String {
    return this.removeAny(" ", "\"")
}


fun String.toUnderLineCase(): String {
    val sb = StringBuilder()
    for ((index, char) in this.withIndex()) {
        if (index > 0 && char.isUpperCase()) {
            sb.append('_')
        }
        sb.append(char)
    }
    return sb.toString()
}


fun CharSequence.isNumber(): Boolean = matches(Regex("""^-?\d*\.?\d+$"""))

fun String.equalsIgnoreCase(string: String): Boolean {
    val equals = this.equals(string, true)
    return equals
}


fun Any?.toNotEmptyStr(): String {
    if (this == null) {
        return ""
    }
    return this.toString().removeBlankOrQuotation()
}

/**
 * 简化的 KMP 兼容字符串格式化函数
 * 只支持 %.nf 格式的浮点数格式化
 */
fun String.kmpFormat(vararg args: Any?): String {
    var result = this
    var argIndex = 0

    // 简单的 %.1f 替换
    if (result.contains("%.1f") && argIndex < args.size) {
        val formatted = when (val value = args[argIndex++]) {
            is Double -> formatDouble(value, 1)
            is Float -> formatDouble(value.toDouble(), 1)
            is Number -> formatDouble(value.toDouble(), 1)
            else -> value.toString()
        }
        result = result.replace("%.1f", formatted)
    }

    // 简单的 %.0f 替换
    if (result.contains("%.0f") && argIndex < args.size) {
        val formatted = when (val value = args[argIndex++]) {
            is Double -> formatDouble(value, 0)
            is Float -> formatDouble(value.toDouble(), 0)
            is Number -> formatDouble(value.toDouble(), 0)
            else -> value.toString()
        }
        result = result.replace("%.0f", formatted)
    }

    return result
}

/**
 * 格式化双精度浮点数到指定小数位数
 */
private fun formatDouble(value: Double, decimals: Int): String {
    if (decimals == 0) {
        return kotlin.math.round(value).toLong().toString()
    }

    // 手动计算10的n次方，避免使用pow函数
    var multiplier = 1.0
    repeat(decimals) {
        multiplier *= 10.0
    }

    val rounded = kotlin.math.round(value * multiplier) / multiplier

    // 手动构建小数位数
    val intPart = rounded.toLong()
    val fracPart = kotlin.math.abs(rounded - intPart)

    if (fracPart == 0.0) {
        return "$intPart.${"0".repeat(decimals)}"
    }

    val fracStr = (fracPart * multiplier).toLong().toString().padStart(decimals, '0')
    return "$intPart.$fracStr"
}

/**
 * 便捷的数字格式化函数
 */
fun Double.formatDecimal(decimals: Int = 2): String {
    return formatDouble(this, decimals)
}

fun Float.formatDecimal(decimals: Int = 2): String {
    return formatDouble(this.toDouble(), decimals)
}

/**
 * 格式化为货币显示（保留指定小数位，默认0位）
 */
fun Double.formatCurrency(decimals: Int = 0): String {
    return formatDouble(this, decimals)
}

fun Float.formatCurrency(decimals: Int = 0): String {
    return formatDouble(this.toDouble(), decimals)
}

/**
 * KMP算法工具类
 * 实现Knuth-Morris-Pratt字符串搜索算法，用于高效字符串匹配
 */
class KMPUtil(pattern: String) {
    private val pattern = pattern
    private val lps: IntArray = computeLPSArray(pattern)

    /**
     * 计算模式串的最长相等前后缀数组(LPS数组)
     * LPS (Longest Proper Prefix which is also Suffix)
     */
    private fun computeLPSArray(pattern: String): IntArray {
        val length = pattern.length
        val lps = IntArray(length)
        var len = 0
        var i = 1

        while (i < length) {
            when {
                pattern[i] == pattern[len] -> {
                    len++
                    lps[i] = len
                    i++
                }

                len != 0 -> {
                    len = lps[len - 1]
                }

                else -> {
                    lps[i] = 0
                    i++
                }
            }
        }
        return lps
    }

    /**
     * 使用KMP算法搜索模式串
     * @param text 文本串
     * @return 匹配的起始位置，未找到返回-1
     */
    fun search(text: String): Int {
        val m = pattern.length
        val n = text.length
        var i = 0 // text的索引
        var j = 0 // pattern的索引

        while (i < n) {
            when {
                pattern[j] == text[i] -> {
                    j++
                    i++
                }
            }

            if (j == m) {
                return i - j // 找到匹配
            } else if (i < n && pattern[j] != text[i]) {
                if (j != 0) {
                    j = lps[j - 1]
                } else {
                    i++
                }
            }
        }
        return -1 // 未找到匹配
    }

    /**
     * 查找所有匹配位置
     */
    fun searchAll(text: String): List<Int> {
        val matches = mutableListOf<Int>()
        val m = pattern.length
        val n = text.length
        var i = 0
        var j = 0

        while (i < n) {
            when {
                pattern[j] == text[i] -> {
                    j++
                    i++
                }
            }

            if (j == m) {
                matches.add(i - j)
                j = lps[j - 1]
            } else if (i < n && pattern[j] != text[i]) {
                if (j != 0) {
                    j = lps[j - 1]
                } else {
                    i++
                }
            }
        }
        return matches
    }
}

/**
 * KMP字符串搜索扩展函数
 * 使用KMP算法检查字符串是否包含指定模式
 */
fun String.containsKMP(pattern: String): Boolean {
    return KMPUtil(pattern).search(this) != -1
}

/**
 * 使用KMP算法查找子串首次出现位置
 * @param pattern 要查找的模式串
 * @return 首次匹配的起始位置，未找到返回-1
 */
fun String.indexOfKMP(pattern: String): Int {
    return KMPUtil(pattern).search(this)
}

/**
 * 使用KMP算法查找所有匹配位置
 * @param pattern 要查找的模式串
 * @return 所有匹配位置的列表
 */
fun String.findAllKMP(pattern: String): List<Int> {
    return KMPUtil(pattern).searchAll(this)
}

/**
 * 使用KMP算法替换字符串中的所有匹配项
 * @param pattern 要替换的模式串
 * @param replacement 替换字符串
 * @return 替换后的字符串
 */
fun String.replaceKMP(pattern: String, replacement: String): String {
    val indices = findAllKMP(pattern)
    if (indices.isEmpty()) return this

    val result = StringBuilder()
    var lastIndex = 0

    indices.forEach { index ->
        result.append(this, lastIndex, index)
        result.append(replacement)
        lastIndex = index + pattern.length
    }

    result.append(this, lastIndex, this.length)
    return result.toString()
}


/**
 * 删除字符串中最后一次出现的指定字符。
 *
 * @param str 字符串
 * @param ch 要删除的字符
 * @return 删除指定字符后的字符串
 */
fun removeLastCharOccurrence(str: String?, ch: Char): String {
    if (str.isNullOrBlank()) {
        return ""
    }
    val lastIndex = str.lastIndexOf(ch) // 获取指定字符最后一次出现的位置
    return if (lastIndex != -1) {
        // 如果找到了指定字符，则删除它
        str.take(lastIndex) + str.substring(lastIndex + 1)
    } else {
        // 如果没有找到指定字符，则返回原字符串
        str
    }
}


/**
 * 变量名类型
 */
enum class VariableType {
    /** 常量: 大写+下划线 (如: MAX_VALUE) */
    CONSTANT,

    /** 小驼峰变量名 (如: firstName) */
    CAMEL_CASE,

    /** ���驼峰类名 (如: UserInfo) */
    PASCAL_CASE,

    /** 下划线分隔 (如: user_name) */
    SNAKE_CASE,

    /** 中划线分隔 (如: user-name) */
    KEBAB_CASE
}

/**
 * 生成合法的变量名
 * @param input 输入字符串
 * @param type 变量名类型
 * @param prefix 前缀(可选)
 * @param suffix 后缀(可选)
 * @return 处理后的变量名
 */
fun toValidVariableName(
    input: String, type: VariableType = VariableType.CAMEL_CASE, prefix: String = "", suffix: String = ""
): String {
    if (input.isBlank()) return ""

    // 检查是否为纯数字
    if (input.all { it.isDigit() }) {
        return "__${input}"  // 纯数字加双下划线前缀
    }

    // 1. 清理特殊字符，只保留字母、数字、空格、下划线、中划线
    var result = input.replace(Regex("[^a-zA-Z0-9\\s_-]"), "")

    if (result.isBlank()) {
        return input
    }
    // 2. 处理数字开头的情况
    if (result.first().isDigit()) {
        result = "_$result"
    }

    // 3. 分词处理（按空格、下划线、中划线分割）
    val words = result.split(Regex("[\\s_-]+")).filter { it.isNotBlank() }.map { it.lowercase() }

    // 4. 根据类型格式化
    result = when (type) {
        VariableType.CONSTANT -> {
            words.joinToString("_") { it.uppercase() }
        }

        VariableType.CAMEL_CASE -> {
            words.first() + words.drop(1).joinToString("") { it.capitalize() }
        }

        VariableType.PASCAL_CASE -> {
            words.joinToString("") { it.capitalize() }
        }

        VariableType.SNAKE_CASE -> {
            words.joinToString("_") { it.lowercase() }
        }

        VariableType.KEBAB_CASE -> {
            words.joinToString("-") { it.lowercase() }
        }
    }

    // 5. 添加前缀和后缀
    if (prefix.isNotBlank()) {
        result = when (type) {
            VariableType.CONSTANT -> "${prefix.uppercase()}_$result"
            VariableType.CAMEL_CASE -> prefix.lowercase() + result.capitalize()
            VariableType.PASCAL_CASE -> prefix.capitalize() + result
            VariableType.SNAKE_CASE -> "${prefix.lowercase()}_$result"
            VariableType.KEBAB_CASE -> "${prefix.lowercase()}-$result"
        }
    }

    if (suffix.isNotBlank()) {
        result = when (type) {
            VariableType.CONSTANT -> "${result}_${suffix.uppercase()}"
            VariableType.CAMEL_CASE -> result + suffix.capitalize()
            VariableType.PASCAL_CASE -> result + suffix.capitalize()
            VariableType.SNAKE_CASE -> "${result}_${suffix.lowercase()}"
            VariableType.KEBAB_CASE -> "${result}-${suffix.lowercase()}"
        }
    }

    return result
}

// 只使用扩展函数形式
fun String.toConstantName(prefix: String = "", suffix: String = "") =
    toValidVariableName(this, VariableType.CONSTANT, prefix, suffix)

fun String.toCamelCase(prefix: String = "", suffix: String = "") =
    toValidVariableName(this, VariableType.CAMEL_CASE, prefix, suffix)

fun String.toPascalCase(prefix: String = "", suffix: String = "") =
    toValidVariableName(this, VariableType.PASCAL_CASE, prefix, suffix)

fun String.toSnakeCase(prefix: String = "", suffix: String = "") =
    toValidVariableName(this, VariableType.SNAKE_CASE, prefix, suffix)

fun String.toKebabCase(prefix: String = "", suffix: String = "") =
    toValidVariableName(this, VariableType.KEBAB_CASE, prefix, suffix)

fun String.capitalize() = replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }

/**
 * 获取字符串长度
 */
fun length(str: String?): Int {
    return str?.length ?: 0
}

/**
 * 检查字符序列是否包含任意一个给定的测试字符序列
 *
 * @param testStrs 要检查的测试字符序列集合
 * @return 如果包含任意一个测试字符序列则返回true，否则返回false
 */
fun CharSequence?.containsAny(vararg testStrs: CharSequence): Boolean {
    if (this.isNullOrEmpty() || testStrs.isEmpty()) {
        return false
    }

    for (testStr in testStrs) {
        if (this!!.contains(testStr)) {
            return true
        }
    }

    return false
}

fun CharSequence?.isBlank(): Boolean {
    return this == null || this.trim().isEmpty()
}

fun CharSequence?.isNullOrEmpty(): Boolean {
    return this == null || this.isEmpty()
}

/**
 * 删除多余符号
 * @param [source]
 * @param [duplicateElement]
 * @return [String?]
 */
fun removeDuplicateSymbol(source: String?, duplicateElement: String?): String? {
    if (source.isNullOrEmpty() || duplicateElement.isNullOrEmpty()) {
        return source
    }
    val sb = StringBuilder()
    var previous = "" // 初始化前一个元素，用于比较
    var i = 0
    while (i < source.length) {
        val elementLength = duplicateElement.length
        if (i + elementLength <= source.length && source.substring(i, i + elementLength) == duplicateElement) {
            if (previous != duplicateElement) {
                sb.append(duplicateElement)
                previous = duplicateElement
            }
            i += elementLength
        } else {
            sb.append(source[i])
            previous = source[i].toString()
            i++
        }
    }
    return sb.toString()
}


/**
 * 提取p标签环绕的字符串
 * @param [input]
 * @return [List<String>]
 */
fun extractTextBetweenPTags(input: String?): List<String> {
    if (input.isNullOrEmpty()) return emptyList()

    // 判断字符串是否包含 <p> 标签
    if ("<p>" !in input || "</p>" !in input) {
        return emptyList() // 如果没有 <p> 标签，返回空列表
    }

    // 定义正则表达式，用来匹配 <p> 和 </p> 之间的内容
    val regex = Regex("<p>(.*?)</p>")

    // 提取所有匹配的内容
    return regex.findAll(input).map { it.groupValues[1] }.toList()
}

/**
 * 清理文档注释，移除注释符号并格式化内容
 * @param docComment 文档注释字符串
 * @return 清理后的字符串
 */

fun cleanDocComment(docComment: String?): String {
    if (docComment == null) return ""

    // 使用正则表达式去除注释符号和多余空格
    val trim = docComment.replace(Regex("""/\*\*?"""), "")  // 去除开头的 /* 或 /**
        .replace(Regex("""\*"""), "")      // 去除行内的 *
        .replace(Regex("""\*/"""), "")     // 去除结尾的 */
        .replace(Regex("""/"""), "")     // 去除结尾的 */
        .replace(Regex("""\n"""), " ")      // 将换行替换为空格
        .replace(Regex("""\s+"""), " ")    // 合并多个空格为一个
        .trim()

    return trim                             // 去除首尾空格
}


/**
 * 从字符串数组中找到第一个非空字符串
 * @param strings 字符串数组
 * @return 第一个非空字符串，如果都为空则返回空字符串
 */
fun firstNotBlank(vararg strings: String?): String {
    return strings.firstOrNull { !it.isNullOrBlank() } ?: ""
}

/**
 * 扩展函数：提取REST URL
 */
fun String?.getRestUrl(): String {
    if (this.isNullOrBlank()) {
        return ""
    }

    val pattern = Regex(".*:\\d+(/[^/]+)(/.*)")
    val matchResult = pattern.find(this)

    return if (matchResult != null && matchResult.groupValues.size > 2) {
        matchResult.groupValues[2]
    } else {
        ""
    }
}

/**
 * wasm测试失败
 * @param [input]
 * @return [MutableMap<String?, Any?>]
 */
fun extractKeyValuePairs(input: String): MutableMap<String?, Any?> {
    val keyValueMap = HashMap<String?, Any?>()
    // 正则表达式匹配形如 "key : value" 或 "key：value" 的子串，考虑冒号和值周围的空白字符
    val pattern = Regex("([\\p{L}\\p{N}_]+)[ \\t]*([:：])[ \\t]*([\\p{L}\\p{N}_]+)")

    pattern.findAll(input)
        .forEach { match ->
            // 提取匹配到的key和value，去除空白字符
            val key = match.groupValues[1].trim()
            val value = match.groupValues[3].trim()
            keyValueMap[key] = value
        }

    return keyValueMap
}

fun String.escapeSpecialCharacters(): String {
    return this
        // 基础转义字符
//            .replace("\\", "\\\\")  // 反斜杠
//            .replace("\"", "\\\"")  // 双引号
//            .replace("\b", "\\b")   // 退格
//            .replace("\n", "\\n")   // 换行
//            .replace("\r", "\\r")   // 回车
//            .replace("\t", "\\t")   // 制表符
        .replace("\u000C", "\\f") // 换页
        .replace("'", "\\'")    // 单引号

        // 正则表达式特殊字符
        .replace(Regex("[\\[\\]{}()^$.|*+?]")) { "\\${it.value}" }

        // Unicode 字符
        .replace(Regex("[\\x00-\\x1F\\x7F]")) { "\\u${it.value[0].code.toString(16).padStart(4, '0')}" }

        // HTML/XML 特殊字符
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("&", "&amp;")

        // SQL 注入防护字符
        .replace(";", "\\;")
        .replace("--", "\\--")
        .replace("/*", "\\/\\*")
        .replace("*/", "*\\/")

        // Shell 特殊字符
        .replace("`", "\\`")
        .replace("$", "\\$")
        .replace("!", "\\!")

        // 其他常见特殊字符
        .replace("%", "\\%")
//            .replace("@", "\\@")
        .replace("#", "\\#")
        .replace("~", "\\~")
        .replace("=", "\\=")
        .replace("+", "\\+")

        // 控制字符
        .replace(Regex("\\p{Cntrl}")) { "\\u${it.value[0].code.toString(16).padStart(4, '0')}" }
}

/**
 * 将字符串转换为 kebab-case 格式
 * 例如: "helloWorld" -> "hello-world"
 *      "HelloWorld" -> "hello-world"
 *      "hello_world" -> "hello-world"
 *      "hello world" -> "hello-world"
 * @param this@toKebabCase 输入字符串
 * @return kebab-case 格式字符串
 */
@Suppress("unused")
fun String.toKebabCase(): String {
    if (isEmpty()) return this

    // 使用正则表达式处理驼峰命名和下划线分隔
    return replace(Regex("(?<!^)([A-Z])"), "-$1")
        // 将下划线和空格替换为连字符
        .replace(Regex("[_\\s]+"), "-")
        // 转换为小写
        .lowercase()
        // 移除多余的连字符
        .replace(Regex("-{2,}"), "-")
        // 移除开头和结尾的连字符
        .trim('-')
}

fun String.makeSurroundWithBrackets(): String {
    return this.addSuffixIfNot("(").addSuffixIfNot(")")
}
