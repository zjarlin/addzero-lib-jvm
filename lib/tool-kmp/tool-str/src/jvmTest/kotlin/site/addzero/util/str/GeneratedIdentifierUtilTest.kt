package site.addzero.util.str

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * 验证 JVM 侧生成标识符的行为。
 */
class GeneratedIdentifierUtilTest {

    @Test
    /**
     * 中文描述应在 JVM 侧自动转成方法名。
     */
    fun shouldGenerateMethodNameFromChineseText() {
        assertEquals("duQuBanKaKuaiZhao", "读取板卡快照".toGeneratedMethodName())
    }

    @Test
    /**
     * 中文描述应在 JVM 侧自动转成属性名。
     */
    fun shouldGeneratePropertyNameFromChineseText() {
        assertEquals("wenDuSheZhi", "温度设置".toGeneratedPropertyName())
    }

    @Test
    /**
     * 空输入应回退到默认类型名。
     */
    fun shouldFallbackToDefaultTypeName() {
        assertEquals("GeneratedMethod", "".toGeneratedTypeName(defaultName = "GeneratedMethod"))
    }

    @Test
    /**
     * 数字开头的文本应补齐合法前缀。
     */
    fun shouldPrefixDigitStartedIdentifier() {
        assertEquals("_1wireState", "1wire state".toGeneratedPropertyName())
    }

    @Test
    /**
     * 混合驼峰文本应正确拆词。
     */
    fun shouldSplitAcronymAndCamelCase() {
        assertEquals("xmlHttpRequest", "XMLHttpRequest".toGeneratedMethodName())
        assertEquals("XmlHttpRequest", "XMLHttpRequest".toGeneratedTypeName())
    }
}
