package site.addzero.biz.spec.iot.tdengine;

import org.junit.jupiter.api.Test;
import site.addzero.biz.spec.iot.IotThingRef;
import site.addzero.biz.spec.iot.TelemetryReport;
import site.addzero.biz.spec.iot.TelemetryValue;
import site.addzero.biz.spec.iot.spi.IotPropertySpecProviders;
import site.addzero.biz.spec.iot.spi.TdengineTypeMappings;
import site.addzero.biz.spec.iot.spi.TelemetryTableNamingStrategies;
import site.addzero.biz.spec.iot.test.spi.CustomTelemetryNamingStrategy;
import site.addzero.biz.spec.iot.test.spi.TestPropertySpecProvider;

import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TdengineTelemetrySqlBuilderTest {

    @Test
    void shouldBuildCreateMigrationInsertAndQueries() throws Exception {
        Path tempDir = Files.createTempDirectory("spec-iot-sql");
        writeService(tempDir, "site.addzero.biz.spec.iot.spi.IotPropertySpecProvider", TestPropertySpecProvider.class.getName());
        writeService(tempDir, "site.addzero.biz.spec.iot.spi.TelemetryTableNamingStrategy", CustomTelemetryNamingStrategy.class.getName());

        ClassLoader original = Thread.currentThread().getContextClassLoader();
        URLClassLoader loader = new URLClassLoader(new URL[]{tempDir.toUri().toURL()}, original);
        try {
            Thread.currentThread().setContextClassLoader(loader);
            IotPropertySpecProviders.reload();
            TelemetryTableNamingStrategies.reload();
            TdengineTypeMappings.reload();

            IotThingRef schemaRef = IotThingRef.of("product", "demo-product");
            IotThingRef sourceRef = IotThingRef.of("device", "device-1");
            TdengineTelemetrySqlBuilder builder = new TdengineTelemetrySqlBuilder();

            SqlStatement createStable = builder.buildCreateStable(schemaRef);
            assertEquals(
                    "CREATE STABLE custom_stable_demo_product (ts TIMESTAMP, report_time TIMESTAMP, temperature FLOAT, running BOOL) TAGS (device_id NCHAR(128))",
                    createStable.getSql()
            );

            java.util.List<SqlStatement> migration = builder.buildSchemaMigration(schemaRef, Collections.<TdColumnSpec>emptyList());
            assertEquals(1, migration.size());
            assertEquals(createStable.getSql(), migration.get(0).getSql());

            TelemetryReport report = TelemetryReport.builder()
                    .schemaRef(schemaRef)
                    .sourceRef(sourceRef)
                    .reportTime(LocalDateTime.of(2026, 3, 16, 12, 30))
                    .addValue(new TelemetryValue("temperature", site.addzero.biz.spec.iot.IotValueType.FLOAT32, 23.5f))
                    .addValue(new TelemetryValue("running", site.addzero.biz.spec.iot.IotValueType.BOOLEAN, Boolean.TRUE))
                    .build();

            SqlStatement insert = builder.buildInsert(report);
            assertEquals(
                    "INSERT INTO custom_sub_device_1 USING custom_stable_demo_product TAGS (?) (ts, report_time, temperature, running) VALUES (NOW, ?, ?, ?)",
                    insert.getSql()
            );
            assertEquals("device-1", insert.getParameters().get(0));
            assertEquals(4, insert.getParameters().size());

            SqlStatement history = builder.buildHistoryQuery(
                    new TelemetryHistoryQuery(
                            schemaRef,
                            sourceRef,
                            "temperature",
                            LocalDateTime.of(2026, 3, 16, 0, 0),
                            LocalDateTime.of(2026, 3, 17, 0, 0)
                    )
            );
            assertTrue(history.getSql().contains("FROM custom_sub_device_1"));
            assertTrue(history.getSql().contains("temperature AS value"));
            assertEquals(2, history.getParameters().size());

            SqlStatement latest = builder.buildLatestQuery(schemaRef, "temperature");
            assertEquals(
                    "SELECT device_id, ts AS update_time, LAST(temperature) AS value FROM custom_stable_demo_product GROUP BY device_id",
                    latest.getSql()
            );

            TdSchemaDiff diff = builder.planSchemaDiff(
                    schemaRef,
                    Arrays.asList(
                            TdengineSchemaDefaults.tsColumn(),
                            TdengineSchemaDefaults.reportTimeColumn(),
                            TdengineSchemaDefaults.deviceIdTagColumn(),
                            new TdColumnSpec("temperature", TdColumnSpec.TYPE_INT, null, null)
                    )
            );
            assertEquals(Collections.singletonList(new TdColumnSpec("running", TdColumnSpec.TYPE_BOOL, null, null)), diff.getAddedColumns());
            assertEquals(Collections.singletonList(new TdColumnSpec("temperature", TdColumnSpec.TYPE_FLOAT, null, null)), diff.getRecreatedColumns());
        } finally {
            Thread.currentThread().setContextClassLoader(original);
            IotPropertySpecProviders.reload();
            TelemetryTableNamingStrategies.reload();
            TdengineTypeMappings.reload();
            loader.close();
        }
    }

    private static void writeService(Path root, String serviceName, String content) throws Exception {
        Path file = root.resolve("META-INF/services/" + serviceName);
        Files.createDirectories(file.getParent());
        Files.write(file, Collections.singleton(content), StandardCharsets.UTF_8);
    }
}
