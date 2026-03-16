package site.addzero.ddlgenerator.lsi

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import site.addzero.ddlgenerator.core.model.AutoDdlLogicalType
import site.addzero.ddlgenerator.lsi.support.TestAnnotation
import site.addzero.ddlgenerator.lsi.support.TestClass
import site.addzero.ddlgenerator.lsi.support.TestField
import site.addzero.ddlgenerator.lsi.support.TestType

class LsiAutoDdlSchemaAdapterTest {

    @Test
    fun `converts scalar columns foreign keys indexes and sequences`() {
        val customer = TestClass(
            simpleName = "Customer",
            qualifiedName = "demo.Customer",
            annotations = listOf(entity()),
            fields = listOf(
                TestField(
                    name = "id",
                    type = TestType("Long"),
                    typeName = "Long",
                    annotations = listOf(id())
                ),
                TestField(
                    name = "code",
                    type = TestType("String"),
                    typeName = "String",
                    annotations = listOf(key(group = "biz")),
                    columnName = "customer_code",
                )
            )
        )
        val order = TestClass(
            simpleName = "OrderRecord",
            qualifiedName = "demo.OrderRecord",
            annotations = listOf(entity(), table(name = "order_record")),
            comment = "订单",
            fields = listOf(
                TestField(
                    name = "id",
                    type = TestType("Long"),
                    typeName = "Long",
                    annotations = listOf(id(), generatedValue(strategy = "SEQUENCE", generatorName = "order_seq"))
                ),
                TestField(
                    name = "amount",
                    type = TestType("BigDecimal"),
                    typeName = "BigDecimal",
                    annotations = listOf(column(precision = 10, scale = 2))
                ),
                TestField(
                    name = "customer",
                    type = TestType("Customer", qualifiedName = "demo.Customer", lsiClass = customer),
                    typeName = "Customer",
                    fieldTypeClass = customer,
                    annotations = listOf(manyToOne(), joinColumn("customer_id"))
                )
            )
        )

        val schema = LsiAutoDdlSchemaAdapter.from(listOf(order, customer))
        val orderTable = schema.table("order_record")
        assertNotNull(orderTable)
        assertEquals(3, orderTable.columns.size)
        assertEquals(AutoDdlLogicalType.DECIMAL, orderTable.column("amount")?.logicalType)
        assertEquals("customer_id", orderTable.column("customer_id")?.name)
        assertEquals("customer", orderTable.foreignKeys.single().referencedTableName)
        assertEquals("order_seq", schema.sequences.single().name)

        val customerTable = schema.table("customer")
        assertNotNull(customerTable)
        assertEquals("uk_customer_biz", customerTable.indexes.single().name)
    }

    @Test
    fun `scans owning many to many into pure junction table`() {
        val role = TestClass(
            simpleName = "Role",
            qualifiedName = "demo.Role",
            annotations = listOf(entity()),
            fields = listOf(TestField(name = "id", type = TestType("Long"), typeName = "Long", annotations = listOf(id())))
        )
        val user = TestClass(
            simpleName = "UserAccount",
            qualifiedName = "demo.UserAccount",
            annotations = listOf(entity()),
            fields = listOf(
                TestField(name = "id", type = TestType("Long"), typeName = "Long", annotations = listOf(id())),
                TestField(
                    name = "roles",
                    type = TestType(
                        simpleName = "List",
                        qualifiedName = "kotlin.collections.List",
                        isCollectionType = true,
                        typeParameters = listOf(TestType("Role", qualifiedName = "demo.Role", lsiClass = role))
                    ),
                    typeName = "List",
                    isCollectionType = true,
                    annotations = listOf(
                        manyToMany(),
                        joinTable(name = "user_role", joinColumnName = "user_id", inverseJoinColumnName = "role_id")
                    )
                )
            )
        )

        val junctionTables = LsiAutoDdlSchemaAdapter.scanManyToManyTables(listOf(user, role))
        val junction = junctionTables.single()
        assertEquals("user_role", junction.name)
        assertEquals(listOf("user_id", "role_id"), junction.columns.map { it.name })
        assertTrue(junction.foreignKeys.any { it.referencedTableName == "user_account" })
        assertTrue(junction.foreignKeys.any { it.referencedTableName == "role" })
    }

    private fun entity(): TestAnnotation {
        return TestAnnotation("org.babyfish.jimmer.sql.Entity", "Entity")
    }

    private fun table(name: String): TestAnnotation {
        return TestAnnotation("org.babyfish.jimmer.sql.Table", "Table", mapOf("name" to name))
    }

    private fun id(): TestAnnotation {
        return TestAnnotation("org.babyfish.jimmer.sql.Id", "Id")
    }

    private fun key(group: String? = null): TestAnnotation {
        return TestAnnotation("org.babyfish.jimmer.sql.Key", "Key", buildMap {
            if (group != null) {
                put("group", group)
            }
        })
    }

    private fun column(
        precision: Int? = null,
        scale: Int? = null,
    ): TestAnnotation {
        return TestAnnotation("org.babyfish.jimmer.sql.Column", "Column", buildMap {
            if (precision != null) {
                put("precision", precision)
            }
            if (scale != null) {
                put("scale", scale)
            }
        })
    }

    private fun generatedValue(strategy: String, generatorName: String): TestAnnotation {
        return TestAnnotation(
            "org.babyfish.jimmer.sql.GeneratedValue",
            "GeneratedValue",
            mapOf("strategy" to strategy, "generatorName" to generatorName)
        )
    }

    private fun manyToOne(): TestAnnotation {
        return TestAnnotation("org.babyfish.jimmer.sql.ManyToOne", "ManyToOne")
    }

    private fun joinColumn(name: String): TestAnnotation {
        return TestAnnotation(
            "org.babyfish.jimmer.sql.JoinColumn",
            "JoinColumn",
            mapOf("name" to name, "referencedColumnName" to "id")
        )
    }

    private fun manyToMany(): TestAnnotation {
        return TestAnnotation("org.babyfish.jimmer.sql.ManyToMany", "ManyToMany")
    }

    private fun joinTable(
        name: String,
        joinColumnName: String,
        inverseJoinColumnName: String,
    ): TestAnnotation {
        return TestAnnotation(
            "org.babyfish.jimmer.sql.JoinTable",
            "JoinTable",
            mapOf(
                "name" to name,
                "joinColumnName" to joinColumnName,
                "inverseJoinColumnName" to inverseJoinColumnName,
            )
        )
    }
}
