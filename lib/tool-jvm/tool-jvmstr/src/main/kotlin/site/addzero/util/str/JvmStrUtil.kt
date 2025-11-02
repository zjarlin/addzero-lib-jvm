//import kotlin.text.String
//
//
///**
// *标记为代码块
// * @param [markdown]
// * @return [String]
// */
//fun extractMarkdownBlockContent(markdown: String?): String {
//    if (markdown.isNullOrEmpty()) return ""
//
//    if (markdown.containsAny("json", "```")) {
//        val regex = Regex("```\\w*\\s*(.*?)\\s*```", setOf(RegexOption.DOT_MATCHES_ALL))
//        val matchResult = regex.find(markdown)
//        return matchResult?.groups?.get(1)?.value?.trim() ?: ""
//    }
//
//    return markdown
//}
//
//
///**
// * 优化表名
// * @param tableEnglishName
// * @param tableChineseName
// * @return [String]
// */
//fun shortEng(tableEnglishName: String, tableChineseName: String?): String {
//    var tableEnglishName = tableEnglishName
//    if (tableEnglishName.length > 15) {
//        tableEnglishName = PinyinUtil.getFirstLetter(tableChineseName, "")
//    }
//    tableEnglishName = StrUtil.removeAny(tableEnglishName, "(", ")")
//    tableEnglishName = tableEnglishName.replace("\\((.*?)\\)".toRegex(), "") // 移除括号及其内容
//    tableEnglishName = tableEnglishName.replace("(_{2,})".toRegex(), "_") // 移除连续的下划线
//    return tableEnglishName
//}
//
//
