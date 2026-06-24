package site.addzero.ddlgenerator.dialect.postgresql

import kotlin.test.Test
import kotlin.test.assertEquals
import site.addzero.ddlgenerator.core.diff.AlterColumn
import site.addzero.ddlgenerator.core.diff.CreateTable
import site.addzero.ddlgenerator.core.model.AutoDdlColumn
import site.addzero.ddlgenerator.core.model.AutoDdlLogicalType
import site.addzero.ddlgenerator.core.model.AutoDdlTable

class PostgreSqlAutoDdlDialectTest {

    @Test
    fun `renders create table with single column primary key`() {
        val dialect = PostgreSqlAutoDdlDialect()
        val statements = dialect.render(
            listOf(
                CreateTable(
                    AutoDdlTable(
                        name = "book",
                        columns = listOf(
                            AutoDdlColumn("id", AutoDdlLogicalType.INT64, nullable = false, primaryKey = true),
                            AutoDdlColumn("title", AutoDdlLogicalType.STRING, nullable = false, length = 128),
                        )
                    )
                )
            )
        )

        assertEquals(
            listOf(
                """
                CREATE TABLE "book" (
                  "id" BIGINT NOT NULL,
                  "title" VARCHAR(128) NOT NULL,
                  PRIMARY KEY ("id")
                );
                """.trimIndent()
            ),
            statements
        )
    }

    @Test
    fun `renders alter column as multiple statements`() {
        val dialect = PostgreSqlAutoDdlDialect()
        val statements = dialect.render(
            listOf(
                AlterColumn(
                    tableName = "book",
                    column = AutoDdlColumn("title", AutoDdlLogicalType.STRING, nullable = false, length = 128, defaultValue = "'N/A'")
                )
            )
        )

        assertEquals(
            listOf(
                """ALTER TABLE "book" ALTER COLUMN "title" TYPE VARCHAR(128);""",
                """ALTER TABLE "book" ALTER COLUMN "title" SET NOT NULL;""",
                """ALTER TABLE "book" ALTER COLUMN "title" SET DEFAULT 'N/A';""",
            ),
            statements
        )
    }
}
