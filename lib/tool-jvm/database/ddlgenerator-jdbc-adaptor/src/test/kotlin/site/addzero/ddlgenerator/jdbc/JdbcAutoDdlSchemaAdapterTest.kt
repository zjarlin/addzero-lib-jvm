package site.addzero.ddlgenerator.jdbc

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import site.addzero.ddlgenerator.core.model.AutoDdlIndexType
import site.addzero.ddlgenerator.core.model.AutoDdlLogicalType
import site.addzero.entity.ForeignKeyMetadata
import site.addzero.entity.JdbcColumnMetadata
import site.addzero.entity.JdbcIndexMetadata
import site.addzero.entity.JdbcTableMetadata
import java.sql.Types

class JdbcAutoDdlSchemaAdapterTest {

    @Test
    fun `converts tables columns indexes and foreign keys`() {
        val schema = JdbcAutoDdlSchemaAdapter.from(
            tables = listOf(
                JdbcTableMetadata(
                    tableName = "orders",
                    schema = "PUBLIC",
                    tableType = "TABLE",
                    remarks = "订单表",
                    columns = listOf(
                        JdbcColumnMetadata("orders", "id", Types.BIGINT, "bigint", null, false, "NO", "", null, true),
                        JdbcColumnMetadata("orders", "amount", Types.DECIMAL, "decimal", null, false, "NO", "金额", "0", false),
                        JdbcColumnMetadata("orders", "payload", Types.OTHER, "jsonb", null, true, "YES", "", null, false),
                    )
                )
            ),
            foreignKeys = listOf(
                ForeignKeyMetadata(
                    pkTableName = "customer",
                    pkColumnName = "id",
                    fkTableName = "orders",
                    fkColumnName = "customer_id",
                    keySeq = 1,
                    fkName = "fk_orders_customer",
                    pkName = "pk_customer",
                )
            ),
            indexes = listOf(
                JdbcIndexMetadata(
                    tableName = "orders",
                    name = "uk_orders_amount",
                    columnNames = listOf("amount"),
                    unique = true,
                )
            )
        )

        val orders = schema.table("orders")
        assertNotNull(orders)
        assertEquals("订单表", orders.comment)
        assertEquals(AutoDdlLogicalType.DECIMAL, orders.column("amount")?.logicalType)
        assertEquals(AutoDdlLogicalType.JSON, orders.column("payload")?.logicalType)
        assertEquals("customer", orders.foreignKeys.single().referencedTableName)
        assertEquals(AutoDdlIndexType.UNIQUE, orders.indexes.single().type)
    }
}
