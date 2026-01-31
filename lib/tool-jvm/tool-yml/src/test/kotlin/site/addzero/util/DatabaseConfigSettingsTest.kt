package site.addzero.util

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class DatabaseConfigSettingsTest {

    @Test
    fun `extracts schema from mysql jdbc url`() {
        val config = DatabaseConfigSettings(
            jdbcUrl = "jdbc:mysql://localhost:3306/my_database?useSSL=false",
            jdbcUsername = "root",
            jdbcPassword = "password"
        )
        assertEquals("my_database", config.schema)
    }

    @Test
    fun `extracts schema from postgresql jdbc url`() {
        val config = DatabaseConfigSettings(
            jdbcUrl = "jdbc:postgresql://localhost:5432/postgres_db",
            jdbcUsername = "postgres",
            jdbcPassword = "password"
        )
        assertEquals("postgres_db", config.schema)
    }

    @Test
    fun `returns null when jdbcUrl is null`() {
        val config = DatabaseConfigSettings(
            jdbcUrl = null,
            jdbcUsername = "root",
            jdbcPassword = "password"
        )
        assertEquals(null, config.schema)
    }

    @Test
    fun `returns null when jdbcUrl is empty`() {
        val config = DatabaseConfigSettings(
            jdbcUrl = "",
            jdbcUsername = "root",
            jdbcPassword = "password"
        )
        assertEquals(null, config.schema)
    }

    @Test
    fun `extracts schema when no parameters in url`() {
        val config = DatabaseConfigSettings(
            jdbcUrl = "jdbc:mysql://localhost:3306/simple_db",
            jdbcUsername = "root",
            jdbcPassword = "password"
        )
        assertEquals("simple_db", config.schema)
    }
}
