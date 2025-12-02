package site.addzero.util.str

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrUtilTest {

    @Test
    fun testExtractKeyValuePairs() {
        // 测试基本的键值对提取
        val input1 = "name: John age: 30 city: NewYork"
        val result1 = extractKeyValuePairs(input1)
        assertEquals(3, result1.size)
        assertEquals("John", result1["name"])
        assertEquals("30", result1["age"])
        assertEquals("NewYork", result1["city"])

        // 测试带中文冒号的键值对提取
        val input2 = "姓名：张三 年龄：25 城市：北京"
        val result2 = extractKeyValuePairs(input2)
        assertEquals(3, result2.size)
        assertEquals("张三", result2["姓名"])
        assertEquals("25", result2["年龄"])
        assertEquals("北京", result2["城市"])

        // 测试混合空格和制表符的情况
        val input3 = "key1 :\tvalue1\tkey2:  value2  key3  ： value3"
        val result3 = extractKeyValuePairs(input3)
        assertEquals(3, result3.size)
        assertEquals("value1", result3["key1"])
        assertEquals("value2", result3["key2"])
        assertEquals("value3", result3["key3"])

        // 测试无匹配的情况
        val input4 = "no key value pairs here"
        val result4 = extractKeyValuePairs(input4)
        assertTrue(result4.isEmpty())

        // 测试空字符串
        val input5 = ""
        val result5 = extractKeyValuePairs(input5)
        assertTrue(result5.isEmpty())
    }

    @Test
    fun testToKebabCaseFirstImplementation() {
        // 测试第一个实现
        assertEquals("hello-world", "helloWorld".toKebabCaseFirstImpl())
        assertEquals("hello-world", "HelloWorld".toKebabCaseFirstImpl())
        assertEquals("hello-world", "hello_world".toKebabCaseFirstImpl())
        assertEquals("hello-world", "hello world".toKebabCaseFirstImpl())
        assertEquals("h-e-l-l-o-w-o-r-l-d", "HELLO_WORLD".toKebabCaseFirstImpl())
        assertEquals("", "".toKebabCaseFirstImpl())
        assertEquals("a", "A".toKebabCaseFirstImpl())
        assertEquals("a-bc", "aBc".toKebabCaseFirstImpl())
        assertEquals("x-m-l-http-request", "XMLHttpRequest".toKebabCaseFirstImpl())
    }

    @Test
    fun testToKebabCaseSecondImplementation() {
        // 测试第二个实现
        assertEquals("hello-world", "helloWorld".toKebabCaseSecondImpl())
        assertEquals("hello-world", "HelloWorld".toKebabCaseSecondImpl())
        assertEquals("hello_world", "hello_world".toKebabCaseSecondImpl())
        assertEquals("hello space", "hello space".toKebabCaseSecondImpl())
        assertEquals("hello_world", "HELLO_WORLD".toKebabCaseSecondImpl())
        assertEquals("", "".toKebabCaseSecondImpl())
        assertEquals("a", "A".toKebabCaseSecondImpl())
        assertEquals("a-bc", "aBc".toKebabCaseSecondImpl())
        assertEquals("xmlhttp-request", "XMLHttpRequest".toKebabCaseSecondImpl())
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
}