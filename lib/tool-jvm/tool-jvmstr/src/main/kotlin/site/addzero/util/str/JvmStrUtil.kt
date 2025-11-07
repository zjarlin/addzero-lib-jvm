package site.addzero.util.str

import java.io.File
/**
 * 优化表名
 * @param tableEnglishName
 * @param tableChineseName
 * @return [String]
 */
fun defaultTableEnglishName(tableEnglishName: String, tableChineseName: String?): String {
    var tableEnglishName = tableEnglishName

    if (tableChineseName.isNotBlank()&&tableEnglishName.isBlank()) {
        tableEnglishName=PinYin4JUtils.hanziToPinyin(tableChineseName!!,"_")
    }
    tableEnglishName = removeAny(tableEnglishName, "(", ")")
    tableEnglishName = tableEnglishName.replace("\\((.*?)\\)".toRegex(), "") // 移除括号及其内容
    tableEnglishName = tableEnglishName.replace("(_{2,})".toRegex(), "_") // 移除连续的下划线
    return tableEnglishName
}

fun String.getParentPathAndmkdir(childPath: String): String {
    val parentFile = File(this).parentFile
    val targetDir = File(parentFile, childPath)
    if (!targetDir.exists()) {
        targetDir.mkdirs()
    }
    return targetDir.path
}

/**
 *标记为代码块
 * @param [markdown]
 * @return [String]
 */
fun extractMarkdownBlockContent(markdown: String?): String? {
    if (markdown.isNullOrEmpty()) return ""
    if (markdown?.contains("json") == true || markdown?.contains("```") == true) {
        val regex = Regex("```\\w*\\s*(.*?)\\s*```", setOf(RegexOption.DOT_MATCHES_ALL))
        val matchResult = markdown?.let { regex.find(it) }
        return matchResult?.groups?.get(1)?.value?.trim() ?: ""
    }
    return markdown
}

/**
 *提取代码块中的内容
 * @param [code]
 * @return [String]
 */
fun extractCodeBlockContent(code: String): String {
    val regex = Regex("``\\w*\\s*(.*?)\\s*``", RegexOption.DOT_MATCHES_ALL)
    val matchResult = regex.find(code)
    return matchResult?.groups?.get(1)?.value?.trim() ?: ""
}
