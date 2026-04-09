@file:JvmName("JvmStrUtil")

package site.addzero.util.jvmstr

/**
 * 兼容旧包名，实际实现已迁到 tool-str。
 */
fun defaultTableEnglishName(tableEnglishName: String, tableChineseName: String?): String {
    return site.addzero.util.str.defaultTableEnglishName(tableEnglishName, tableChineseName)
}

/**
 * 兼容旧包名，实际实现已迁到 tool-str。
 */
fun String.getParentPathAndMkdir(childPath: String): String {
    return site.addzero.util.str.getParentPathAndMkdir(this, childPath)
}

/**
 * 兼容旧包名，实际实现已迁到 tool-str。
 */
fun extractMarkdownBlockContent(markdown: String?): String {
    return site.addzero.util.str.extractMarkdownBlockContent(markdown)
}

/**
 * 兼容旧包名，实际实现已迁到 tool-str。
 */
fun extractCodeBlockContent(code: String): String {
    return site.addzero.util.str.extractCodeBlockContent(code)
}
