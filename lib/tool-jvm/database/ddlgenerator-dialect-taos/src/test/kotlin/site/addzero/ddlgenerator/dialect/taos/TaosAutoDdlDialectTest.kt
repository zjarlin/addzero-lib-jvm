package site.addzero.ddlgenerator.dialect.taos

import kotlin.test.Test
import kotlin.test.assertEquals
import site.addzero.ddlgenerator.core.diff.CreateTable
import site.addzero.ddlgenerator.core.model.AutoDdlColumn
import site.addzero.ddlgenerator.core.model.AutoDdlLogicalType
import site.addzero.ddlgenerator.core.model.AutoDdlTable

class TaosAutoDdlDialectTest {

    @Test
    fun `renders taos regular table subset`() {
        val dialect = TaosAutoDdlDialect()
        val statements = dialect.render(
            listOf(
                CreateTable(
                    AutoDdlTable(
                        name = "device_log",
                        columns = listOf(
                            AutoDdlColumn("ts", AutoDdlLogicalType.TIMESTAMP, nullable = false),
                            AutoDdlColumn("device_id", AutoDdlLogicalType.STRING, nullable = false, length = 64),
                            AutoDdlColumn("temperature", AutoDdlLogicalType.FLOAT64),
                        )
                    )
                )
            )
        )

        assertEquals(
            listOf(
                """
                CREATE TABLE device_log (
                  ts TIMESTAMP NOT NULL,
                  device_id VARCHAR(64) NOT NULL,
                  temperature DOUBLE
                );
                """.trimIndent()
            ),
            statements
        )
    }
}
