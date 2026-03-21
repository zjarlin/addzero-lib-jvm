package site.addzero.example

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class I18NExampleTest {

    @Test
    fun `rewrites string literals to i18n lookups`() {
        assertEquals(expectedHello(), helloMessage())
        assertEquals(expectedGoodbye(), farewellMessage())
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
