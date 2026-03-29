package site.addzero.example

import org.junit.jupiter.api.Test
import site.addzero.util.I8nutil
import kotlin.test.assertEquals

class I18NExampleTest {

    @Test
    fun `rewrites string literals to i18n lookups`() {
        I8nutil.setLocale("en")
        try {
            assertEquals(expectedHello(), helloMessage())
            assertEquals(expectedGoodbye(), farewellMessage())
        } finally {
            I8nutil.clearLocale()
        }
    }

    @Test
    fun `falls back to source text when locale resource is missing`() {
        I8nutil.setLocale("ja")
        try {
            assertEquals("你好", helloMessage())
            assertEquals("再见", farewellMessage())
        } finally {
            I8nutil.clearLocale()
        }
    }

    private fun expectedHello(): String {
        return buildString {
            append('h')
            append('e')
            append('l')
            append('l')
            append('o')
        }
    }

    private fun expectedGoodbye(): String {
        return buildString {
            append('g')
            append('o')
            append('o')
            append('d')
            append('b')
            append('y')
            append('e')
        }
    }
}
