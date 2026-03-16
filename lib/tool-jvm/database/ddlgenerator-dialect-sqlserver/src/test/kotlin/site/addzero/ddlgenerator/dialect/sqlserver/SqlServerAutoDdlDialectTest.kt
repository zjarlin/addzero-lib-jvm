package site.addzero.ddlgenerator.dialect.sqlserver

import kotlin.test.Test
import kotlin.test.assertEquals
import site.addzero.ddlgenerator.core.diff.CreateTable
import site.addzero.ddlgenerator.core.model.AutoDdlColumn
import site.addzero.ddlgenerator.core.model.AutoDdlLogicalType
import site.addzero.ddlgenerator.core.model.AutoDdlTable

class SqlServerAutoDdlDialectTest {

    @Test
    fun `renders sql server identity create table`() {
        val dialect = SqlServerAutoDdlDialect()
        val statements = dialect.render(
            listOf(
                CreateTable(
                    AutoDdlTable(
                        name = "invoice",
                        columns = listOf(
                            AutoDdlColumn("id", AutoDdlLogicalType.INT64, nullable = false, primaryKey = true, autoIncrement = true),
                            AutoDdlColumn("code", AutoDdlLogicalType.STRING, nullable = false, length = 64),
                        )
                    )
                )
            )
        )

        assertEquals(
            listOf(
                """
                CREATE TABLE [invoice] (
                  [id] BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
                  [code] NVARCHAR(64) NOT NULL
                );
                """.trimIndent()
            ),
            statements
        )
    }
}
