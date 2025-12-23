package site.addzero.util

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class DatabaseConfigReaderTest {

    @TempDir
    lateinit var tempDir: File

    @Test
    fun `reads standard single datasource configuration`() {
        writeApplicationYaml(
            """
            spring:
              datasource:
                url: jdbc:mysql://localhost:3306/app
                username: root
                password: secret
            """.trimIndent()
        )

        val config = DatabaseConfigReader.fromSpringYml(customYmlPath = tempDir.absolutePath)

        assertNotNull(config)
        assertEquals("jdbc:mysql://localhost:3306/app", config.jdbcUrl)
        assertEquals("root", config.jdbcUsername)
        assertEquals("secret", config.jdbcPassword)
    }

    @Test
    fun `uses preferred named datasource when provided`() {
        writeApplicationYaml(
            """
            spring:
              datasource:
                master:
                  url: jdbc:mysql://localhost:3306/master
                  username: root
                  password: masterPass
                slave:
                  url: jdbc:mysql://localhost:3306/slave
                  username: slaveUser
                  password: slavePass
            """.trimIndent()
        )

        val config = DatabaseConfigReader.fromSpringYml(
            customYmlPath = tempDir.absolutePath,
            preferDataSourceName = "slave"
        )

        assertNotNull(config)
        assertEquals("jdbc:mysql://localhost:3306/slave", config.jdbcUrl)
        assertEquals("slaveUser", config.jdbcUsername)
        assertEquals("slavePass", config.jdbcPassword)
    }

    @Test
    fun `falls back to common named datasource when single datasource not present`() {
        writeApplicationYaml(
            """
            spring:
              datasource:
                master:
                  url: jdbc:mysql://localhost:3306/master
                  username: masterUser
                  password: masterPass
                other:
                  url: jdbc:mysql://localhost:3306/other
                  username: otherUser
                  password: otherPass
            """.trimIndent()
        )

        val config = DatabaseConfigReader.fromSpringYml(customYmlPath = tempDir.absolutePath)

        assertNotNull(config)
        assertEquals("jdbc:mysql://localhost:3306/master", config.jdbcUrl)
        assertEquals("masterUser", config.jdbcUsername)
        assertEquals("masterPass", config.jdbcPassword)
    }

    private fun writeApplicationYaml(content: String) {
        File(tempDir, "application.yml").writeText(content)
    }
}
