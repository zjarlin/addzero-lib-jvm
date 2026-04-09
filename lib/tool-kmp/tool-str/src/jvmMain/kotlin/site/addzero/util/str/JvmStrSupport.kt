package site.addzero.util.str

import PinYin4JUtils
import java.io.File

/**
 * 优化表英文名。
 *
 * @param tableEnglishName 表英文名。
 * @param tableChineseName 表中文名。
 */
fun defaultTableEnglishName(
    tableEnglishName: String,
    tableChineseName: String?,
): String {
    var result = tableEnglishName
    if (!tableChineseName.isNullOrBlank() && tableEnglishName.isBlank()) {
        result = PinYin4JUtils.hanziToPinyin(tableChineseName, "_")
    }
    result = result.replace("(", "").replace(")", "")
    result = result.replace("\\((.*?)\\)".toRegex(), "")
    result = result.replace("(_{2,})".toRegex(), "_")
    return result
}

/**
 * 获取父路径并创建子目录。
 *
 * @param childPath 子目录路径。
 */
fun String.getParentPathAndMkdir(
    childPath: String,
): String {
    val parentFile = File(this).parentFile
    val targetDir = File(parentFile, childPath)
    if (!targetDir.exists()) {
        targetDir.mkdirs()
    }
    return targetDir.path
}

/**
 * 提取 Markdown 代码块中的内容。
 *
 * @param markdown Markdown 文本。
 */
fun extractMarkdownBlockContent(
    markdown: String?,
): String {
    val content = markdown ?: return ""
    if (content.isEmpty()) {
        return ""
    }
    if (content.contains("json") || content.contains("```")) {
        val regex = Regex("```\\w*\\s*(.*?)\\s*```", setOf(RegexOption.DOT_MATCHES_ALL))
        val matchResult = regex.find(content)
        return matchResult?.groups?.get(1)?.value?.trim() ?: ""
    }
    return content
}

/**
 * 提取代码块中的内容。
 *
 * @param code 代码文本。
 */
fun extractCodeBlockContent(
    code: String,
): String {
    val regex = Regex("``\\w*\\s*(.*?)\\s*``", RegexOption.DOT_MATCHES_ALL)
    val matchResult = regex.find(code)
    return matchResult?.groups?.get(1)?.value?.trim() ?: ""
}
