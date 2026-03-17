package site.addzero.biz.spec.iot.tdengine

import org.junit.jupiter.api.Test
import site.addzero.biz.spec.iot.IotThingRef
import site.addzero.biz.spec.iot.IotValueType
import site.addzero.biz.spec.iot.TelemetryReport
import site.addzero.biz.spec.iot.TelemetryValue
import site.addzero.biz.spec.iot.spi.IotPropertySpecProviders
import site.addzero.biz.spec.iot.spi.TdengineTypeMappings
import site.addzero.biz.spec.iot.spi.TelemetryTableNamingStrategies
import site.addzero.biz.spec.iot.test.spi.CustomTelemetryNamingStrategy
import site.addzero.biz.spec.iot.test.spi.TestPropertySpecProvider
import java.net.URLClassLoader
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TdengineTelemetrySqlBuilderTest {

    @Test
    fun shouldBuildCreateMigrationInsertAndQueries() {
        val tempDir = Files.createTempDirectory("spec-iot-sql")
        writeService(
            tempDir,
            "site.addzero.biz.spec.iot.spi.IotPropertySpecProvider",
            TestPropertySpecProvider::class.java.name,
        )
        writeService(
            tempDir,
            "site.addzero.biz.spec.iot.spi.TelemetryTableNamingStrategy",
            CustomTelemetryNamingStrategy::class.java.name,
        )

        val original = Thread.currentThread().contextClassLoader
        val loader = URLClassLoader(arrayOf(tempDir.toUri().toURL()), original)
        try {
            Thread.currentThread().contextClassLoader = loader
            IotPropertySpecProviders.reload()
            TelemetryTableNamingStrategies.reload()
            TdengineTypeMappings.reload()

            val schemaRef = IotThingRef.of("product", "demo-product")
            val sourceRef = IotThingRef.of("device", "device-1")
            val builder = TdengineTelemetrySqlBuilder()

            val createStable = builder.buildCreateStable(schemaRef)
            assertEquals(
                "CREATE STABLE custom_stable_demo_product (ts TIMESTAMP, report_time TIMESTAMP, temperature FLOAT, running BOOL) TAGS (device_id NCHAR(128))",
                createStable.sql,
            )

            val migration = builder.buildSchemaMigration(schemaRef, emptyList())
            assertEquals(1, migration.size)
            assertEquals(createStable.sql, migration[0].sql)

            val report = TelemetryReport.builder()
                .schemaRef(schemaRef)
                .sourceRef(sourceRef)
                .reportTime(LocalDateTime.of(2026, 3, 16, 12, 30))
                .addValue(TelemetryValue("temperature", IotValueType.FLOAT32, 23.5f))
                .addValue(TelemetryValue("running", IotValueType.BOOLEAN, true))
                .build()

            val insert = builder.buildInsert(report)
            assertEquals(
                "INSERT INTO custom_sub_device_1 USING custom_stable_demo_product TAGS (?) (ts, report_time, temperature, running) VALUES (NOW, ?, ?, ?)",
                insert.sql,
            )
            assertEquals("device-1", insert.parameters[0])
            assertEquals(4, insert.parameters.size)

            val history = builder.buildHistoryQuery(
                TelemetryHistoryQuery(
                    schemaRef,
                    sourceRef,
                    "temperature",
                    LocalDateTime.of(2026, 3, 16, 0, 0),
                    LocalDateTime.of(2026, 3, 17, 0, 0),
                ),
            )
            assertTrue(history.sql.contains("FROM custom_sub_device_1"))
            assertTrue(history.sql.contains("temperature AS value"))
            assertEquals(2, history.parameters.size)

            val latest = builder.buildLatestQuery(schemaRef, "temperature")
            assertEquals(
                "SELECT device_id, ts AS update_time, LAST(temperature) AS value FROM custom_stable_demo_product GROUP BY device_id",
                latest.sql,
            )

            val diff = builder.planSchemaDiff(
                schemaRef,
                listOf(
                    TdengineSchemaDefaults.tsColumn(),
                    TdengineSchemaDefaults.reportTimeColumn(),
                    TdengineSchemaDefaults.deviceIdTagColumn(),
                    TdColumnSpec("temperature", TdColumnSpec.TYPE_INT, null, null),
                ),
            )
            assertEquals(
                listOf(TdColumnSpec("running", TdColumnSpec.TYPE_BOOL, null, null)),
                diff.addedColumns,
            )
            assertEquals(
                listOf(TdColumnSpec("temperature", TdColumnSpec.TYPE_FLOAT, null, null)),
                diff.recreatedColumns,
            )
        } finally {
            Thread.currentThread().contextClassLoader = original
            IotPropertySpecProviders.reload()
            TelemetryTableNamingStrategies.reload()
            TdengineTypeMappings.reload()
            loader.close()
        }
    }

    private fun writeService(root: Path, serviceName: String, content: String) {
        val file = root.resolve("META-INF/services/$serviceName")
        Files.createDirectories(file.parent)
        Files.write(file, listOf(content), StandardCharsets.UTF_8)
    }
}
