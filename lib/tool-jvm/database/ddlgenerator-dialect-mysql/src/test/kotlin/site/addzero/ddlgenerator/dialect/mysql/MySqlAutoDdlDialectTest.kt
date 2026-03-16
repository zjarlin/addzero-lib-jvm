package site.addzero.ddlgenerator.dialect.mysql

import kotlin.test.Test
import kotlin.test.assertEquals
import site.addzero.ddlgenerator.core.diff.CreateTable
import site.addzero.ddlgenerator.core.model.AutoDdlColumn
import site.addzero.ddlgenerator.core.model.AutoDdlLogicalType
import site.addzero.ddlgenerator.core.model.AutoDdlTable

class MySqlAutoDdlDialectTest {

    @Test
    fun `renders create table`() {
        val dialect = MySqlAutoDdlDialect()
        val statements = dialect.render(
            listOf(
                CreateTable(
                    AutoDdlTable(
                        name = "book",
                        columns = listOf(
                            AutoDdlColumn("id", AutoDdlLogicalType.INT64, nullable = false, primaryKey = true, autoIncrement = true),
                            AutoDdlColumn("title", AutoDdlLogicalType.STRING, nullable = false, length = 128),
                        )
                    )
                )
            )
        )

        assertEquals(
            listOf(
                """
                CREATE TABLE `book` (
                  `id` BIGINT AUTO_INCREMENT NOT NULL PRIMARY KEY,
                  `title` VARCHAR(128) NOT NULL
                );
                """.trimIndent()
            ),
            statements
        )
    }
}
