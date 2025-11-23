@file:JvmName("JvmStrUtil")

package site.addzero.util.jvmstr

import PinYin4JUtils
import java.io.File

/**
 * 优化表名
 * @param tableEnglishName 表的英文名
 * @param tableChineseName 表的中文名
 * @return 优化后的表名
 */
@Suppress("unused")
fun defaultTableEnglishName(tableEnglishName: String, tableChineseName: String?): String {
    var result = tableEnglishName

    // 修复问题1: 使用安全调用
    if (!tableChineseName.isNullOrBlank() && tableEnglishName.isBlank()) {
        result = PinYin4JUtils.hanziToPinyin(tableChineseName, "_")
    }
    result = result.replace("(", "").replace(")", "")
    result = result.replace("\\((.*?)\\)".toRegex(), "") // 移除括号及其内容
    result = result.replace("(_{2,})".toRegex(), "_") // 移除连续的下划线
    return result
}

/**
 * 获取父路径并创建子目录
 * @param childPath 子目录路径
 * @return 创建的目录路径
 */
@Suppress("unused")
fun String.getParentPathAndMkdir(childPath: String): String {
    val parentFile = File(this).parentFile
    val targetDir = File(parentFile, childPath)
    if (!targetDir.exists()) {
        targetDir.mkdirs()
    }
    return targetDir.path
}

/**
 * 提取 Markdown 代码块中的内容
 * @param markdown Markdown 文本
 * @return 提取的内容，如果没有代码块则返回原文本
 */
@Suppress("unused")
fun extractMarkdownBlockContent(markdown: String?): String {
    // 修复问题5: 改为返回 String 而不是 String?
    if (markdown.isNullOrEmpty()) return ""

    // 修复问题6、7: 移除不必要的安全调用
    if (markdown.contains("json") || markdown.contains("```")) {
        val regex = Regex("```\\w*\\s*(.*?)\\s*```", setOf(RegexOption.DOT_MATCHES_ALL))
        val matchResult = regex.find(markdown)
        return matchResult?.groups?.get(1)?.value?.trim() ?: ""
    }
    return markdown
}

/**
 * 提取代码块中的内容
 * @param code 代码文本
 * @return 提取的内容
 */
@Suppress("unused")
fun extractCodeBlockContent(code: String): String {
    val regex = Regex("``\\w*\\s*(.*?)\\s*``", RegexOption.DOT_MATCHES_ALL)
    val matchResult = regex.find(code)
    return matchResult?.groups?.get(1)?.value?.trim() ?: ""
}
