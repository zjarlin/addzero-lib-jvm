/**
 *标记为代码块
 * @param [markdown]
 * @return [String]
 */
fun extractMarkdownBlockContent(markdown: String?): String {
    if (markdown.isNullOrEmpty()) return ""

    if (markdown.containsAny("json", "```")) {
        val regex = Regex("```\\w*\\s*(.*?)\\s*```", setOf(RegexOption.DOT_MATCHES_ALL))
        val matchResult = regex.find(markdown)
        return matchResult?.groups?.get(1)?.value?.trim() ?: ""
    }

    return markdown
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
 *标记为代码块
 * @param [markdown]
 * @return [String]
 */
fun extractMarkdownBlockContent(markdown: String?): String {
    if (markdown.isNullOrEmpty()) return ""

    if (markdown.containsAny("json", "```")) {
        val regex = Regex("```\\w*\\s*(.*?)\\s*```", setOf(RegexOption.DOT_MATCHES_ALL))
        val matchResult = regex.find(markdown)
        return matchResult?.groups?.get(1)?.value?.trim() ?: ""
    }

    return markdown
}



/**
 * 优化表名
 * @param tableEnglishName
 * @param tableChineseName
 * @return [String]
 */
fun shortEng(tableEnglishName: String, tableChineseName: String?): String {
    var tableEnglishName = tableEnglishName
    if (tableEnglishName.length > 15) {
        tableEnglishName = PinyinUtil.getFirstLetter(tableChineseName, "")
    }
    tableEnglishName = StrUtil.removeAny(tableEnglishName, "(", ")")
    tableEnglishName = tableEnglishName.replace("\\((.*?)\\)".toRegex(), "") // 移除括号及其内容
    tableEnglishName = tableEnglishName.replace("(_{2,})".toRegex(), "_") // 移除连续的下划线
    return tableEnglishName
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
