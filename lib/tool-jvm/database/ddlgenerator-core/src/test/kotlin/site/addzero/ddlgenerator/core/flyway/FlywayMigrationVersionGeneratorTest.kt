package site.addzero.ddlgenerator.core.flyway

import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class FlywayMigrationVersionGeneratorTest {
    private val generator = FlywayMigrationVersionGenerator(
        clock = Clock.fixed(
            Instant.parse("2026-08-17T08:30:45.123Z"),
            ZoneId.of("Asia/Shanghai"),
        )
    )

    @Test
    fun `generates timestamp version after legacy v1000 migrations`() {
        val resolution = generator.generate(
            FlywayMigrationVersionRequest(
                appliedVersions = listOf("1000.1482471961.20260805182746"),
                namespace = ":lib:biz:iot:model",
                sql = "ALTER TABLE iot_device ADD COLUMN handler_name TEXT;",
            )
        )

        assertTrue(resolution.version.startsWith("20260817.163045.123.0."))
        assertTrue(parseVersion(resolution.version) > parseVersion("1000.1482471961.20260805182746"))
        assertFalse(resolution.reusedExisting)
    }

    @Test
    fun `does not reuse same sql migration below applied version`() {
        val resolution = generator.generate(
            FlywayMigrationVersionRequest(
                appliedVersions = listOf("20260814.000000"),
                existingMigrations = listOf(
                    ExistingFlywayMigration(
                        version = "1000.1482471961.12561950961564532770",
                        sql = "ALTER TABLE iot_device ADD COLUMN handler_name TEXT;",
                    )
                ),
                namespace = ":lib:biz:iot:model",
                sql = "ALTER TABLE iot_device ADD COLUMN handler_name TEXT;",
            )
        )

        assertTrue(parseVersion(resolution.version) > parseVersion("20260814.000000"))
        assertFalse(resolution.reusedExisting)
    }

    @Test
    fun `reuses same sql migration that is still pending`() {
        val pendingVersion = "20260817.160000.0.0.1.2"
        val resolution = generator.generate(
            FlywayMigrationVersionRequest(
                appliedVersions = listOf("20260814.000000"),
                existingMigrations = listOf(
                    ExistingFlywayMigration(
                        version = pendingVersion,
                        sql = "ALTER TABLE iot_device ADD COLUMN handler_name TEXT;\r\n",
                    )
                ),
                namespace = ":lib:biz:iot:model",
                sql = "ALTER TABLE iot_device ADD COLUMN handler_name TEXT;\n",
            )
        )

        assertEquals(pendingVersion, resolution.version)
        assertTrue(resolution.reusedExisting)
    }

    @Test
    fun `advances from future known version when build clock is behind`() {
        val latestVersion = "20260818.120000.999.7.8.9"
        val resolution = generator.generate(
            FlywayMigrationVersionRequest(
                appliedVersions = listOf(latestVersion),
                namespace = ":lib:biz:iot:model",
                sql = "ALTER TABLE iot_device ADD COLUMN handler_mobile TEXT;",
            )
        )

        val resolvedVersion = parseVersion(resolution.version)
        assertTrue(resolvedVersion > parseVersion(latestVersion))
        assertEquals("20260818.120000.999.8", resolvedVersion.parts.take(4).joinToString("."))
        assertEquals(5, resolvedVersion.parts.size)
    }

    @Test
    fun `same request and fixed clock produce stable version`() {
        val request = FlywayMigrationVersionRequest(
            appliedVersions = listOf("20260814_000000"),
            namespace = ":lib:biz:iot:model",
            sql = "ALTER TABLE iot_device ADD COLUMN handler_name TEXT;",
        )

        assertEquals(generator.generate(request), generator.generate(request))
    }

    @Test
    fun `namespace and sql both participate in version identity`() {
        val first = generator.generate(
            FlywayMigrationVersionRequest(
                namespace = ":lib:biz:iot:model",
                sql = "ALTER TABLE iot_device ADD COLUMN handler_name TEXT;",
            )
        )
        val second = generator.generate(
            FlywayMigrationVersionRequest(
                namespace = ":lib:biz:message:model",
                sql = "ALTER TABLE iot_device ADD COLUMN handler_name TEXT;",
            )
        )
        val third = generator.generate(
            FlywayMigrationVersionRequest(
                namespace = ":lib:biz:iot:model",
                sql = "ALTER TABLE iot_device ADD COLUMN handler_mobile TEXT;",
            )
        )

        assertNotEquals(first.version, second.version)
        assertNotEquals(first.version, third.version)
    }

    @Test
    fun `generated version fits flyway history varchar 50 column`() {
        val resolution = generator.generate(
            FlywayMigrationVersionRequest(
                appliedVersions = listOf("20260817.160000"),
                existingMigrations = listOf(
                    ExistingFlywayMigration(
                        version = "20260817.170000",
                        sql = "CREATE INDEX demo_idx ON demo(id);",
                    )
                ),
                namespace = ":apps:tianjin:server",
                sql = "ALTER TABLE iot_device ADD COLUMN handler_name TEXT;",
            )
        )

        assertTrue(resolution.version.length <= 50)
        assertTrue(parseVersion(resolution.version) > parseVersion("20260817.170000"))
    }

    @Test
    fun `overlong pending version is replaced by valid higher version`() {
        val overlongVersion = "20260818.120000.999.0.1220147163635573897.1398964062078713089"
        val sql = "ALTER TABLE iot_device ADD COLUMN handler_name TEXT;"
        val resolution = generator.generate(
            FlywayMigrationVersionRequest(
                appliedVersions = listOf("20260817.160000"),
                existingMigrations = listOf(
                    ExistingFlywayMigration(
                        version = overlongVersion,
                        sql = sql,
                    )
                ),
                namespace = ":lib:biz:iot:model",
                sql = sql,
            )
        )

        assertFalse(resolution.reusedExisting)
        assertTrue(resolution.version.length <= 50)
        assertTrue(parseVersion(resolution.version) > parseVersion(overlongVersion))
    }
}
