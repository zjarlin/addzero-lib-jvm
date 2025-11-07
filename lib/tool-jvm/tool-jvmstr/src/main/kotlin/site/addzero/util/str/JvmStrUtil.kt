import kotlin.text.String


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
    tableEnglishName = removeAny(tableEnglishName, "(", ")")
    tableEnglishName = tableEnglishName.replace("\\((.*?)\\)".toRegex(), "") // 移除括号及其内容
    tableEnglishName = tableEnglishName.replace("(_{2,})".toRegex(), "_") // 移除连续的下划线
    return tableEnglishName
}


package site.addzero.common.kt_util

import cn.hutool.core.convert.Convert
import cn.hutool.core.io.FileUtil
import cn.hutool.core.util.NumberUtil
import cn.hutool.core.util.StrUtil
import kotlin.text.String

/**
 * 扩展函数：添加前后缀
 */
fun String?.makeSurroundWith(fix: String?): String? {
    return this?.let {
        StrUtil.addPrefixIfNot(it, fix).let {
            StrUtil
                .addSuffixIfNot(it, fix)
        }
    }
}


fun String?.join(conjun: String? = System.lineSeparator(), other: String?): String {
    if (this.isNullOrEmpty()) {
        return ""
    }
    val join = StrUtil.join(conjun, this, other)
    return join
}



/**
 * 扩展函数：用HTML P标签包裹
 */
fun String?.makeSurroundWithHtmlP(): String? {
    if (StrUtil.isBlank(this)) {
        return ""
    }
    return StrUtil.addPrefixIfNot(this, "<p>").let { StrUtil.addSuffixIfNot(it, "</p>") }
}


fun String?.addPrefixIfNot(prefix: String): String {
    if (StrUtil.isBlank(this)) {
        return prefix
    }
    return StrUtil.addPrefixIfNot(this, prefix)
}


fun String.getParentPathAndmkdir(childPath: String): String {
    val parent1 = FileUtil.getParent(this, 1)
    //            val parent2 = FileUtil.getParent(filePath, 2)
    //            val parent3 = FileUtil.getParent(filePath, 0)
    val mkParentDirs = FileUtil.mkdir("$parent1/$childPath")
    //            val canonicalPath = virtualFile.canonicalPath
    //            val parent = psiFile!!.parent
    val filePath1 = mkParentDirs.path
    return filePath1
}


fun CharSequence.toCamelCase(): String {
    return StrUtil.toCamelCase(this)
}

fun CharSequence.removeAny(vararg testStrs: CharSequence): String {
    return StrUtil.removeAny(this, *testStrs)
}

/**
 * 删除空格或者引号
 * @param [testStrs]
 * @return [String]
 */
fun CharSequence.removeBlankOrQuotation(): String {
    return StrUtil.removeAny(this, " ", "\"")
}


fun CharSequence.toUnderlineCase(): String {
    val toUnderlineCase = StrUtil.toUnderlineCase(this)
    return toUnderlineCase
}


fun CharSequence.isNumber(): Boolean {
    return NumberUtil.isNumber(this)
}


fun CharSequence.containsAny(vararg testStrs: CharSequence): Boolean {
    val containsAny = StrUtil.containsAny(this, *testStrs)
    return containsAny
}

fun CharSequence.containsAnyIgnoreCase(vararg testStrs: CharSequence): Boolean {
    val containsAny = StrUtil.containsAnyIgnoreCase(this, *testStrs)
    return containsAny
}


fun Any?.toNotBlankStr(): String {
    if (this == null) {
        return ""
    }
    val toStr1 = Convert.toStr(this)
    return toStr1
}

fun String.equalsIgnoreCase(string: String): Boolean {
    return StrUtil.equalsIgnoreCase(this, string)
}

/**
 *提取markdown代码块中的内容
 * @param [markdown]
 * @return [String]
 */
fun extractMarkdownBlockContent(markdown: String): String {
    val regex = Regex("```\\w*\\s*(.*?)\\s*```", RegexOption.DOT_MATCHES_ALL)
    val matchResult = regex.find(markdown)
    return matchResult?.groups?.get(1)?.value?.trim() ?: ""
}
