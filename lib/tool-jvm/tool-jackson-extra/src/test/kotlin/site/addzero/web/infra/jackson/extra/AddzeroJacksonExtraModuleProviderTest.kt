package site.addzero.web.infra.jackson.extra

import org.junit.jupiter.api.Test
import site.addzero.web.infra.jackson.toJson
import kotlinx.datetime.LocalDateTime as KotlinxLocalDateTime

class AddzeroJacksonExtraModuleProviderTest {
    @Test
    fun `serializes kotlinx local date time with fractional seconds`() {
        val json = Payload(
            kotlinTime = KotlinxLocalDateTime(2026, 6, 18, 10, 20, 30, 123_456_789),
        ).toJson()

        check(json.contains("2026-06-18T10:20:30.123456789"))
    }

    private data class Payload(
        val kotlinTime: KotlinxLocalDateTime,
    )
}
