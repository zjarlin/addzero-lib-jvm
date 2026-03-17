package site.addzero.ddlgenerator.runtime

import kotlin.test.Test
import kotlin.test.assertTrue
import site.addzero.ddlgenerator.core.options.AutoDdlDiffOptions
import site.addzero.ddlgenerator.runtime.support.TestAnnotation
import site.addzero.ddlgenerator.runtime.support.TestClass
import site.addzero.ddlgenerator.runtime.support.TestField
import site.addzero.ddlgenerator.runtime.support.TestType
import site.addzero.util.db.DatabaseType
import java.sql.DriverManager

class AutoDdlRuntimeSmokeTest {

    @Test
    fun `generates h2 schema sql through service loader`() {
        val customer = TestClass(
            simpleName = "Customer",
            qualifiedName = "demo.Customer",
            annotations = listOf(entity()),
            fields = listOf(
                TestField(name = "id", type = TestType("Long"), typeName = "Long", annotations = listOf(id())),
                TestField(name = "name", type = TestType("String"), typeName = "String"),
            )
        )
        val order = TestClass(
            simpleName = "OrderRecord",
            qualifiedName = "demo.OrderRecord",
            annotations = listOf(entity(), table("order_record")),
            fields = listOf(
                TestField(name = "id", type = TestType("Long"), typeName = "Long", annotations = listOf(id())),
                TestField(
                    name = "customer",
                    type = TestType("Customer", qualifiedName = "demo.Customer", lsiClass = customer),
                    typeName = "Customer",
                    fieldTypeClass = customer,
                    annotations = listOf(manyToOne(), joinColumn("customer_id"))
                ),
            )
        )

        val sql = AutoDdlRuntime.generate(listOf(order, customer), DatabaseType.H2).joinToString("\n")
        assertTrue(sql.contains("CREATE TABLE \"order_record\""))
        assertTrue(sql.contains("FOREIGN KEY"))
    }

    @Test
    fun `diffs against h2 metadata`() {
        val jdbcUrl = "jdbc:h2:mem:ddlgenerator_runtime;DB_CLOSE_DELAY=-1"
        Class.forName("org.h2.Driver")
        DriverManager.getConnection(jdbcUrl, "sa", "").use { connection ->
            connection.createStatement().use { stmt ->
                stmt.execute("""CREATE TABLE customer (id BIGINT PRIMARY KEY, name VARCHAR(32));""")
            }
        }

        val customer = TestClass(
            simpleName = "Customer",
            qualifiedName = "demo.Customer",
            annotations = listOf(entity()),
            fields = listOf(
                TestField(name = "id", type = TestType("Long"), typeName = "Long", annotations = listOf(id())),
                TestField(name = "name", type = TestType("String"), typeName = "String"),
                TestField(name = "email", type = TestType("String"), typeName = "String", annotations = listOf(key())),
            )
        )

        val result = AutoDdlRuntime.diff(
            lsiClasses = listOf(customer),
            jdbcConfig = AutoDdlJdbcConfig(jdbcUrl = jdbcUrl, jdbcUsername = "sa", jdbcPassword = ""),
            databaseType = DatabaseType.H2,
            options = AutoDdlDiffOptions(),
            includeManyToManyTables = false,
        )

        assertTrue(result.statements.any { it.contains("ADD COLUMN") })
        assertTrue(result.statements.any { it.contains("CREATE UNIQUE INDEX") })
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

    private fun key(): TestAnnotation {
        return TestAnnotation("org.babyfish.jimmer.sql.Key", "Key")
    }

    private fun manyToOne(): TestAnnotation {
        return TestAnnotation("org.babyfish.jimmer.sql.ManyToOne", "ManyToOne")
    }

    private fun joinColumn(name: String): TestAnnotation {
        return TestAnnotation("org.babyfish.jimmer.sql.JoinColumn", "JoinColumn", mapOf("name" to name, "referencedColumnName" to "id"))
    }
}
