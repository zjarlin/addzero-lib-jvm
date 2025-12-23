package site.addzero.rustfs

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class RustfsConfigTest {

    @Test
    fun `default configuration matches rustfs quickstart`() {
        val config = RustfsConfig.default()

        assertEquals("http://addzero.site:9000", config.endpoint)
        assertEquals("rustfsadmin", config.accessKey)
        assertEquals("rustfsadmin", config.secretKey)
        assertEquals("us-east-1", config.region)
    }
}
