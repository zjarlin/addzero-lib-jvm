package site.addzero.ddlgenerator.dialect.h2

import kotlin.test.Test
import kotlin.test.assertEquals
import site.addzero.ddlgenerator.core.diff.CreateTable
import site.addzero.ddlgenerator.core.model.AutoDdlColumn
import site.addzero.ddlgenerator.core.model.AutoDdlLogicalType
import site.addzero.ddlgenerator.core.model.AutoDdlTable

class H2AutoDdlDialectTest {

    @Test
    fun `renders h2 create table with json`() {
        val dialect = H2AutoDdlDialect()
        val statements = dialect.render(
            listOf(
                CreateTable(
                    AutoDdlTable(
                        name = "audit_log",
                        columns = listOf(
                            AutoDdlColumn("id", AutoDdlLogicalType.INT64, nullable = false, primaryKey = true, autoIncrement = true),
                            AutoDdlColumn("payload", AutoDdlLogicalType.JSON, nullable = false),
                        )
                    )
                )
            )
        )

        assertEquals(
            listOf(
                """
                CREATE TABLE "audit_log" (
                  "id" BIGINT AUTO_INCREMENT NOT NULL PRIMARY KEY,
                  "payload" JSON NOT NULL
                );
                """.trimIndent()
            ),
            statements
        )
    }
}
