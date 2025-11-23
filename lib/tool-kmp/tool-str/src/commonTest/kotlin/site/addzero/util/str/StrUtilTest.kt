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
}