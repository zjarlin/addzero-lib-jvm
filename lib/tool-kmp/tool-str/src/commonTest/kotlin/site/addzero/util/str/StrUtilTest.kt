package site.addzero.util.str

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrUtilTest {
    @Test
    fun `test low camelcase`() {
        // 测试下划线格式
        assertEquals("userName", "user_name".toLowCamelCase())
        assertEquals("helloWorld", "hello_world".toLowCamelCase())
        
        // 测试中划线格式
        assertEquals("userName", "user-name".toLowCamelCase())
        assertEquals("helloWorld", "hello-world".toLowCamelCase())
        
        // 测试空格分隔格式
        assertEquals("helloWorld", "hello world".toLowCamelCase())
        assertEquals("userName", "user name".toLowCamelCase())
        
        // 测试混合分隔符
        assertEquals("helloWorldTest", "hello_world-test".toLowCamelCase())
        assertEquals("myVarName", "my-var_name".toLowCamelCase())
        
        // 测试边界情况
        assertEquals("", "".toLowCamelCase())
        assertEquals("a", "a".toLowCamelCase())
        assertEquals("abc", "abc".toLowCamelCase())
        
        // 测试多个连续分隔符
        assertEquals("helloWorld", "hello___world".toLowCamelCase())
        assertEquals("helloWorld", "hello---world".toLowCamelCase())
        assertEquals("helloWorld", "hello   world".toLowCamelCase())
        
        // 测试已经是大驼峰的情况（当前实现：整个单词转小写）
        // 注：如果没有分隔符，会被当作单个单词处理
        assertEquals("username", "UserName".toLowCamelCase())
        assertEquals("helloworld", "HelloWorld".toLowCamelCase())
        
        // 测试全大写
        assertEquals("username", "USERNAME".toLowCamelCase())
        assertEquals("hello", "HELLO".toLowCamelCase())
    }

    @Test
    fun `test big camelcase`() {
        // 测试下划线格式
        val result1 = "sss_sss_saa".toBigCamelCase()
        println("sss_sss_saa.toBigCamelCase() = $result1")
        assertEquals("SssSssSaa", result1, "下划线格式应该转换为大驼峰")
        
        // 测试小驼峰格式
        val result2 = "sAbcd".toBigCamelCase()
        println("sAbcd.toBigCamelCase() = $result2")
        assertEquals("SAbcd", result2, "小驼峰格式首字母应该转大写")
        
        // 测试中划线格式（用户报告失败的用例）
        val result3 = "sa-dasd-aosvdi".toBigCamelCase()
        println("sa-dasd-aosvdi.toBigCamelCase() = $result3")
        assertEquals("SaDasdAosvdi", result3, "中划线格式应该转换为大驼峰")
        
        // 测试空格分隔格式
        assertEquals("HelloWorld", "hello world".toBigCamelCase())
        assertEquals("UserName", "user name".toBigCamelCase())
        
        // 测试混合分隔符
        assertEquals("HelloWorldTest", "hello_world-test".toBigCamelCase())
        assertEquals("MyVarName", "my-var_name".toBigCamelCase())
        
        // 测试边界情况
        assertEquals("", "".toBigCamelCase())
        assertEquals("A", "a".toBigCamelCase())
        assertEquals("Abc", "abc".toBigCamelCase())
        
        // 测试多个连续分隔符
        assertEquals("HelloWorld", "hello___world".toBigCamelCase())
        assertEquals("HelloWorld", "hello---world".toBigCamelCase())
        assertEquals("HelloWorld", "hello   world".toBigCamelCase())
        
        // 测试已经是大驼峰的情况
        assertEquals("UserName", "UserName".toBigCamelCase())
        assertEquals("HelloWorld", "HelloWorld".toBigCamelCase())
    }

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

    @Test
    fun testToPascalCase_SnakeCase() {
        // 测试下划线格式 (snake_case) 转大驼峰
        assertEquals("SysYesNo", "sys_yes_no".toPascalCase())
        assertEquals("UserName", "user_name".toPascalCase())
        assertEquals("HelloWorld", "hello_world".toPascalCase())
    }

    @Test
    fun testToPascalCase_CamelCase() {
        // 测试小驼峰 (camelCase) 转大驼峰
        // 期望: propSource -> PropSource
        val result = "propSource".toPascalCase()
        println("propSource.toPascalCase() = $result")
        // 当前实现可能会失败，因为它不处理没有分隔符的驼峰命名
        assertEquals("PropSource", result, "小驼峰格式应该被正确识别并转换")

        assertEquals("HelloWorld", "helloWorld".toPascalCase())
        assertEquals("MyVariableName", "myVariableName".toPascalCase())
    }

    @Test
    fun testToPascalCase_MixedFormats() {
        // 测试混合格式
        assertEquals("SysYesNo", "SYS_YES_NO".toPascalCase()) // 全大写+下划线
        assertEquals("UserInfo", "UserInfo".toPascalCase())   // 已经是大驼峰
    }

    @Test
    fun testToCamelCase_SnakeCase() {
        // 测试下划线格式转小驼峰
        assertEquals("sysYesNo", "sys_yes_no".toCamelCase())
        assertEquals("userName", "user_name".toCamelCase())
    }
}
