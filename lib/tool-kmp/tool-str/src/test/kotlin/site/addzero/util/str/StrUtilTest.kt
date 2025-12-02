package site.addzero.util.str

import kotlin.test.Test
import kotlin.test.assertEquals

class StrUtilTest {

    @Test
    fun testToKebabCaseFirstImplementation() {
        // 测试第一个实现
        assertEquals("hello-world", "helloWorld".toKebabCaseFirstImpl())
        assertEquals("hello-world", "HelloWorld".toKebabCaseFirstImpl())
        assertEquals("hello-world", "hello_world".toKebabCaseFirstImpl())
        assertEquals("hello-world", "hello world".toKebabCaseFirstImpl())
        assertEquals("hello-world", "HELLO_WORLD".toKebabCaseFirstImpl())
        assertEquals("", "".toKebabCaseFirstImpl())
        assertEquals("a", "A".toKebabCaseFirstImpl())
        assertEquals("a-b-c", "aBc".toKebabCaseFirstImpl())
        assertEquals("xml-http-request", "XMLHttpRequest".toKebabCaseFirstImpl())
    }

    @Test
    fun testToKebabCaseSecondImplementation() {
        // 测试第二个实现
        assertEquals("hello-world", "helloWorld".toKebabCaseSecondImpl())
        assertEquals("hello-world", "HelloWorld".toKebabCaseSecondImpl())
        assertEquals("hello-world", "hello_world".toKebabCaseSecondImpl())
        assertEquals("hello-world", "hello space".toKebabCaseSecondImpl())
        assertEquals("hello-world", "HELLO_WORLD".toKebabCaseSecondImpl())
        assertEquals("", "".toKebabCaseSecondImpl())
        assertEquals("a", "A".toKebabCaseSecondImpl())
        assertEquals("a-bc", "aBc".toKebabCaseSecondImpl())
        assertEquals("xml-http-request", "XMLHttpRequest".toKebabCaseSecondImpl())
    }

    // 为第一个实现创建一个单独的函数用于测试
    private fun String.toKebabCaseFirstImpl(): String {
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

    // 为第二个实现创建一个单独的函数用于测试
    private fun String.toKebabCaseSecondImpl(): String {
        return this.replace(Regex("([a-z])([A-Z])"), "$1-$2").lowercase()
    }
    
    @Test
    fun testExtractKeyValuePairs() {
        val input = "name: John\nage: 30\ncity: New York"
        val result = extractKeyValuePairs(input)
        assertEquals(3, result.size)
        assertEquals("John", result["name"])
        assertEquals("30", result["age"])
        assertEquals("New York", result["city"])
    }
}