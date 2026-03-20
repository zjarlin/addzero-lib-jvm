package site.addzero.example

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class I18NExampleTest {

    @Test
    fun `rewrites string literals to i18n lookups`() {
        assertEquals("hello", helloMessage())
        assertEquals("goodbye", farewellMessage())
    }
}
