package site.addzero.common.util

import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

class ConversionsTest {

    @Test
    fun `getWeek 返回中文星期`() {
        val week = Conversions.getWeek(LocalDate.of(2026, 6, 20))

        assertEquals("周六", week)
    }
}
