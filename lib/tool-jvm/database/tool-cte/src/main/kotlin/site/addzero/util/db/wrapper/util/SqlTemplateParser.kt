package site.addzero.util.db.wrapper.util

import java.util.Date
import java.util.regex.Matcher
import java.util.regex.Pattern

object SqlTemplateParser {
    /**
     * 解析 SQL 模板，不依赖 OGNL，保留 WHERE 关键字
     */
    fun parseWrapperSqlToString(customSqlSegment: String?, paramNameValuePairs: Map<String, Any?>):
            String {
        if (customSqlSegment == null || customSqlSegment.trim { it <= ' ' }.isEmpty()) {
            return "WHERE 1=1"
        }

        // 正则匹配 #{...} 模式
        val pattern = Pattern.compile("#\\{([^}]+)}")
        val matcher = pattern.matcher(customSqlSegment)

        val resultSql = StringBuffer()

        while (matcher.find()) {
            val expression = matcher.group(1)


            // 使用自定义方法解析表达式
            val value = parseExpression(expression, paramNameValuePairs)

            // 格式化值并直接替换
            val formattedValue = formatValueForSql(value)
            matcher.appendReplacement(resultSql, Matcher.quoteReplacement(formattedValue))
        }

        matcher.appendTail(resultSql)

        return resultSql.toString()
    }

    /**
     * 解析表达式，替代 OGNL 的功能
     * 支持简单的属性访问，如 user.name, user.age 等
     */
    private fun parseExpression(expression: String, paramNameValuePairs: Map<String, Any?>): Any? {
        // 处理简单的变量引用，如 ew.paramNameValuePairs.xxx
        if (expression.startsWith("ew.paramNameValuePairs.")) {
            val key = expression.substring("ew.paramNameValuePairs.".length)
            return paramNameValuePairs[key]
        }
        // 直接从 paramNameValuePairs 中获取值
        return paramNameValuePairs[expression]
    }

    /**
     * 格式化值用于 SQL
     */
    private fun formatValueForSql(value: Any?): String {
        if (value == null) {
            return "NULL"
        }

        if (value is String) {
            return "'" + value.toString().replace("'", "''") + "'"
        }

        if (value is Number) {
            return value.toString()
        }

        if (value is Boolean) {
            return value.toString()
        }

        if (value is Date) {
            return "'" + java.sql.Date(value.time) + "'"
        }

        // 默认当作字符串处理
        return "'" + value.toString().replace("'", "''") + "'"
    }
}
