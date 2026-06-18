package site.addzero.web.infra.jackson

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class JacksonUtilTest {
    @Test
    fun `serializes java local date time with fractional seconds`() {
        val payload = DatePayload(
            javaTime = LocalDateTime.of(2026, 6, 18, 10, 20, 30, 123_456_789),
        )

        val json = payload.toJson()

        check(json.contains("2026-06-18T10:20:30.123456789"))
    }

    @Test
    fun `parses object and list extensions`() {
        val item = """{"name":"demo","count":2}""".parseObject<SampleItem>()
        val list = """[{"name":"a","count":1},{"name":"b","count":2}]""".toList<SampleItem>()

        assertEquals(SampleItem("demo", 2), item)
        assertEquals(listOf(SampleItem("a", 1), SampleItem("b", 2)), list)
    }

    private data class DatePayload(
        val javaTime: LocalDateTime,
    )

    private data class SampleItem(
        val name: String,
        val count: Int,
    )
}
