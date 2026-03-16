package site.addzero.ddlgenerator.dialect.dm

import kotlin.test.Test
import kotlin.test.assertEquals
import site.addzero.ddlgenerator.core.diff.CreateTable
import site.addzero.ddlgenerator.core.model.AutoDdlColumn
import site.addzero.ddlgenerator.core.model.AutoDdlLogicalType
import site.addzero.ddlgenerator.core.model.AutoDdlTable

class DmAutoDdlDialectTest {

    @Test
    fun `renders dm identity column`() {
        val dialect = DmAutoDdlDialect()
        val statements = dialect.render(
            listOf(
                CreateTable(
                    AutoDdlTable(
                        name = "member",
                        columns = listOf(
                            AutoDdlColumn("id", AutoDdlLogicalType.INT64, nullable = false, primaryKey = true, autoIncrement = true),
                            AutoDdlColumn("name", AutoDdlLogicalType.STRING, nullable = false, length = 64),
                        )
                    )
                )
            )
        )

        assertEquals(
            listOf(
                """
                CREATE TABLE "member" (
                  "id" BIGINT IDENTITY(1, 1) NOT NULL PRIMARY KEY,
                  "name" VARCHAR(64) NOT NULL
                );
                """.trimIndent()
            ),
            statements
        )
    }
}
