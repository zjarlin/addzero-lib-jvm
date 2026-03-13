package site.addzero.example

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TransformOverloadTest1 {

    @Test
    fun `generates cartesian overloads for sout`() {
        val example: SoutExample = object : SoutExample {
            override fun sout(
                t: T,
                r: R,
            ): String {
                return "sout(t=${t.value}, r=${r.value})"
            }
        }

        assertEquals("sout(t=base, r=1)", example.sout(T("base"), R(1)))
        assertEquals("sout(t=from-s, r=2)", example.sout(S("from-s"), R(2)))
        assertEquals("sout(t=base, r=3)", example.sout(T("base"), G(3)))
        assertEquals("sout(t=from-s, r=4)", example.sout(S("from-s"), G(4)))

        val signatures = SoutExample::class.java.methods
            .filter { method -> method.name == "sout" && method.parameterCount == 2 }
            .map { method -> method.parameterTypes.toList() }
            .toSet()

        assertTrue(listOf(T::class.java, R::class.java) in signatures)
        assertTrue(listOf(S::class.java, R::class.java) in signatures)
        assertTrue(listOf(T::class.java, G::class.java) in signatures)
        assertTrue(listOf(S::class.java, G::class.java) in signatures)
    }
}
