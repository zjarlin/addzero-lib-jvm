package site.addzero.ddlgenerator.dialect.sqlite

import kotlin.test.Test
import kotlin.test.assertEquals
import site.addzero.ddlgenerator.core.diff.CreateTable
import site.addzero.ddlgenerator.core.model.AutoDdlColumn
import site.addzero.ddlgenerator.core.model.AutoDdlForeignKey
import site.addzero.ddlgenerator.core.model.AutoDdlLogicalType
import site.addzero.ddlgenerator.core.model.AutoDdlTable

class SQLiteAutoDdlDialectTest {

    @Test
    fun `renders inline foreign keys in create table`() {
        val dialect = SQLiteAutoDdlDialect()
        val statements = dialect.render(
            listOf(
                CreateTable(
                    AutoDdlTable(
                        name = "orders",
                        columns = listOf(
                            AutoDdlColumn("id", AutoDdlLogicalType.INT64, nullable = false, primaryKey = true, autoIncrement = true),
                            AutoDdlColumn("customer_id", AutoDdlLogicalType.INT64, nullable = false),
                        ),
                        foreignKeys = listOf(
                            AutoDdlForeignKey("fk_orders_customer", listOf("customer_id"), "customer", listOf("id"))
                        )
                    )
                )
            )
        )

        assertEquals(
            listOf(
                """
                CREATE TABLE "orders" (
                  "id" INTEGER AUTOINCREMENT NOT NULL PRIMARY KEY,
                  "customer_id" INTEGER NOT NULL,
                  FOREIGN KEY ("customer_id") REFERENCES "customer" ("id")
                );
                """.trimIndent()
            ),
            statements
        )
    }
}
