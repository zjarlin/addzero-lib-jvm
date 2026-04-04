package site.addzero.example

import kotlin.test.Test
import kotlin.test.assertEquals

class SpreadPackExampleTest {

    @Test
    fun generated_overloads_compile_and_run() {
        assertEquals(
            "form:demo:true:-|wrapper:hello:2:done|alias::3:true",
            invokeSpreadPackExample(),
        )
    }
}
