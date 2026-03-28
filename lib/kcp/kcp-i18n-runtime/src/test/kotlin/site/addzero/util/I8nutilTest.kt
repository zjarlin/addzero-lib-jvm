package site.addzero.util

import kotlin.test.Test
import kotlin.test.assertEquals

class I8nutilTest {

    @Test
    fun `supports raw keys containing spaces`() {
        assertEquals(
            expected = "The Compose module is using the i18n compiler plugin.",
            actual = I8nutil.t(
                key = "DemoTextState_bodyText_text_Compose 模块已经接入国际化编译插件。",
                locale = "en",
                basePath = "i18n-runtime-test",
            ),
        )
    }

    @Test
    fun `keeps compatibility with escaped property keys`() {
        assertEquals(
            expected = "hello world",
            actual = I8nutil.t(
                key = "legacy key",
                locale = "en",
                basePath = "i18n-runtime-test",
            ),
        )
    }
}
