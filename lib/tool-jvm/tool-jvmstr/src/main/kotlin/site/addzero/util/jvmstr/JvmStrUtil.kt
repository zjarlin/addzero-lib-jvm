@file:JvmName("JvmStrUtil")

package site.addzero.util.jvmstr

import java.io.File

/**
 * 兼容旧包名，实际实现已迁到 tool-str。
 */
fun defaultTableEnglishName(tableEnglishName: String, tableChineseName: String?): String {
    return site.addzero.util.str.defaultTableEnglishName(tableEnglishName, tableChineseName)
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
