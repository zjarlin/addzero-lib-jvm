package site.addzero.ddlgenerator.dialect.postgresql

import kotlin.test.Test
import kotlin.test.assertEquals
import site.addzero.ddlgenerator.core.diff.AlterColumn
import site.addzero.ddlgenerator.core.model.AutoDdlColumn
import site.addzero.ddlgenerator.core.model.AutoDdlLogicalType

class PostgreSqlAutoDdlDialectTest {

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
