package site.addzero.ddlgenerator.dialect.mysql

import kotlin.test.Test
import kotlin.test.assertEquals
import site.addzero.ddlgenerator.core.diff.CreateTable
import site.addzero.ddlgenerator.core.diff.DropColumnNotNull
import site.addzero.ddlgenerator.core.diff.SetColumnNotNull
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
                CREATE TABLE IF NOT EXISTS `book` (
                  `id` BIGINT AUTO_INCREMENT NOT NULL PRIMARY KEY,
                  `title` VARCHAR(128) NOT NULL
                );
                """.trimIndent()
            ),
            statements
        )
    }

    @Test
    fun `renders nullable repair statements`() {
        val dialect = MySqlAutoDdlDialect()
        val statements = dialect.render(
            listOf(
                SetColumnNotNull(
                    tableName = "book",
                    column = AutoDdlColumn("title", AutoDdlLogicalType.STRING, nullable = false, length = 128)
                ),
                DropColumnNotNull(tableName = "book", columnName = "subtitle"),
            )
        )

        assertEquals(
            listOf(
                """UPDATE `book` SET `title` = '' WHERE `title` IS NULL;""",
                """
                SET @ddl = (
                  SELECT CONCAT('ALTER TABLE `book` MODIFY COLUMN `title` ', COLUMN_TYPE, ' NOT NULL')
                  FROM information_schema.COLUMNS
                  WHERE TABLE_SCHEMA = DATABASE()
                    AND TABLE_NAME = 'book'
                    AND COLUMN_NAME = 'title'
                );
                """.trimIndent(),
                "PREPARE stmt FROM @ddl;",
                "EXECUTE stmt;",
                "DEALLOCATE PREPARE stmt;",
                """
                SET @ddl = (
                  SELECT CONCAT('ALTER TABLE `book` MODIFY COLUMN `subtitle` ', COLUMN_TYPE, ' NULL')
                  FROM information_schema.COLUMNS
                  WHERE TABLE_SCHEMA = DATABASE()
                    AND TABLE_NAME = 'book'
                    AND COLUMN_NAME = 'subtitle'
                );
                """.trimIndent(),
                "PREPARE stmt FROM @ddl;",
                "EXECUTE stmt;",
                "DEALLOCATE PREPARE stmt;",
            ),
            statements
        )
    }
}
