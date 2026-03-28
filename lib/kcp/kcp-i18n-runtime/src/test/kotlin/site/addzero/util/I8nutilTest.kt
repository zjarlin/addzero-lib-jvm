package site.addzero.util

import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class I8nutilTest {

    @BeforeTest
    fun resetLocale() {
        I8nutil.clearLocale()
    }

    @Test
    fun `supports raw keys containing spaces`() {
        I8nutil.setLocale("en")
        assertEquals(
            expected = "The Compose module is using the i18n compiler plugin.",
            actual = I8nutil.t(
                key = "DemoTextState_bodyText_text_Compose 模块已经接入国际化编译插件。",
                fallback = "Compose 模块已经接入国际化编译插件。",
                basePath = "i18n-runtime-test",
            ),
        )
    }

    @Test
    fun `keeps compatibility with escaped property keys`() {
        I8nutil.setLocale("en")
        assertEquals(
            expected = "hello world",
            actual = I8nutil.t(
                key = "legacy key",
                fallback = "legacy key",
                basePath = "i18n-runtime-test",
            ),
        )
    }

    @Test
    fun `falls back to source text when locale translation is missing`() {
        I8nutil.setLocale("zh-CN")

        assertEquals(
            expected = "你好，KCP",
            actual = I8nutil.t(
                key = "DemoTextState_titleText_text_你好，KCP",
                fallback = "你好，KCP",
                basePath = "i18n-runtime-test",
            ),
        )
    }

    @Test
    fun `falls back from regional locale to language locale`() {
        I8nutil.setLocale("en-US")

        assertEquals(
            expected = "Count Clicks",
            actual = I8nutil.t(
                key = "DemoTextState_buttonText_text_点我切换计数",
                fallback = "点我切换计数",
                basePath = "i18n-runtime-test",
            ),
        )
    }

    @Test
    fun `treats blank translation values as missing`() {
        I8nutil.setLocale("ja")

        assertEquals(
            expected = "当前还没有点击按钮。",
            actual = I8nutil.t(
                key = "DemoTextState_statusText_text_当前还没有点击按钮。",
                fallback = "当前还没有点击按钮。",
                basePath = "i18n-runtime-test",
            ),
        )
    }
}
