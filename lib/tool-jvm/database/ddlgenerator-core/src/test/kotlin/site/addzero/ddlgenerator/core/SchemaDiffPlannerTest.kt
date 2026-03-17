package site.addzero.ddlgenerator.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import site.addzero.ddlgenerator.core.diff.AddColumn
import site.addzero.ddlgenerator.core.diff.AddComment
import site.addzero.ddlgenerator.core.diff.AddForeignKey
import site.addzero.ddlgenerator.core.diff.AlterColumn
import site.addzero.ddlgenerator.core.diff.CreateIndex
import site.addzero.ddlgenerator.core.diff.CreateSequence
import site.addzero.ddlgenerator.core.diff.CreateTable
import site.addzero.ddlgenerator.core.diff.DropColumn
import site.addzero.ddlgenerator.core.diff.DropTable
import site.addzero.ddlgenerator.core.diff.SchemaDiffPlanner
import site.addzero.ddlgenerator.core.model.AutoDdlColumn
import site.addzero.ddlgenerator.core.model.AutoDdlForeignKey
import site.addzero.ddlgenerator.core.model.AutoDdlIndex
import site.addzero.ddlgenerator.core.model.AutoDdlIndexType
import site.addzero.ddlgenerator.core.model.AutoDdlLogicalType
import site.addzero.ddlgenerator.core.model.AutoDdlSchema
import site.addzero.ddlgenerator.core.model.AutoDdlSequence
import site.addzero.ddlgenerator.core.model.AutoDdlTable
import site.addzero.ddlgenerator.core.options.AutoDdlDiffOptions
import site.addzero.ddlgenerator.core.options.AutoDdlOptions

class SchemaDiffPlannerTest {

    @Test
    fun `plans create alter and add operations in stable order`() {
        val desired = AutoDdlSchema(
            tables = listOf(
                AutoDdlTable(
                    name = "author",
                    columns = listOf(
                        AutoDdlColumn("id", AutoDdlLogicalType.INT64, nullable = false, primaryKey = true, autoIncrement = true),
                        AutoDdlColumn("name", AutoDdlLogicalType.STRING, nullable = false, length = 128),
                        AutoDdlColumn("bio", AutoDdlLogicalType.TEXT),
                    )
                ),
                AutoDdlTable(
                    name = "book",
                    columns = listOf(
                        AutoDdlColumn("id", AutoDdlLogicalType.INT64, nullable = false, primaryKey = true, autoIncrement = true),
                        AutoDdlColumn("title", AutoDdlLogicalType.STRING, nullable = false, length = 128),
                    )
                ),
            )
        )
        val actual = AutoDdlSchema(
            tables = listOf(
                AutoDdlTable(
                    name = "author",
                    columns = listOf(
                        AutoDdlColumn("id", AutoDdlLogicalType.INT64, nullable = false, primaryKey = true, autoIncrement = true),
                        AutoDdlColumn("name", AutoDdlLogicalType.STRING, nullable = true, length = 64),
                    )
                )
            )
        )

        val operations = SchemaDiffPlanner.plan(desired, actual, AutoDdlDiffOptions())

        assertEquals(3, operations.size)
        assertIs<CreateTable>(operations[0])
        assertIs<AddColumn>(operations[1])
        assertIs<AlterColumn>(operations[2])
    }

    @Test
    fun `respects destructive flag and exclusion rules`() {
        val desired = AutoDdlSchema(
            tables = listOf(
                AutoDdlTable(
                    name = "customer",
                    columns = listOf(AutoDdlColumn("id", AutoDdlLogicalType.INT64, nullable = false, primaryKey = true))
                )
            )
        )
        val actual = AutoDdlSchema(
            tables = listOf(
                AutoDdlTable(
                    name = "customer",
                    columns = listOf(
                        AutoDdlColumn("id", AutoDdlLogicalType.INT64, nullable = false, primaryKey = true),
                        AutoDdlColumn("debug_flag", AutoDdlLogicalType.BOOLEAN),
                    )
                ),
                AutoDdlTable(
                    name = "flyway_schema_history",
                    columns = listOf(AutoDdlColumn("installed_rank", AutoDdlLogicalType.INT32))
                )
            )
        )

        val nonDestructive = SchemaDiffPlanner.plan(desired, actual, AutoDdlDiffOptions())
        assertFalse(nonDestructive.any { it is DropColumn || it is DropTable })

        val destructive = SchemaDiffPlanner.plan(
            desired,
            actual,
            AutoDdlDiffOptions(
                allowDestructiveChanges = true,
                excludeTables = listOf("flyway_schema_history"),
                excludeColumns = listOf("customer.debug_flag"),
            )
        )
        assertFalse(destructive.any { it is DropTable })
        assertFalse(destructive.any { it is DropColumn })
    }

    @Test
    fun `plans sequences indexes foreign keys and comments`() {
        val desired = AutoDdlSchema(
            tables = listOf(
                AutoDdlTable(
                    name = "invoice",
                    comment = "发票表",
                    columns = listOf(
                        AutoDdlColumn("id", AutoDdlLogicalType.INT64, nullable = false, primaryKey = true, sequenceName = "invoice_seq"),
                        AutoDdlColumn("customer_id", AutoDdlLogicalType.INT64, nullable = false, comment = "客户"),
                    ),
                    indexes = listOf(
                        AutoDdlIndex("uk_invoice_customer", listOf("customer_id"), AutoDdlIndexType.UNIQUE)
                    ),
                    foreignKeys = listOf(
                        AutoDdlForeignKey("fk_invoice_customer", listOf("customer_id"), "customer", listOf("id"))
                    )
                )
            ),
            sequences = listOf(AutoDdlSequence("invoice_seq"))
        )
        val actual = AutoDdlSchema(
            tables = listOf(
                AutoDdlTable(
                    name = "invoice",
                    columns = listOf(
                        AutoDdlColumn("id", AutoDdlLogicalType.INT64, nullable = false, primaryKey = true),
                        AutoDdlColumn("customer_id", AutoDdlLogicalType.INT64, nullable = false),
                    )
                )
            )
        )

        val operations = SchemaDiffPlanner.plan(
            desired,
            actual,
            AutoDdlDiffOptions(ddlOptions = AutoDdlOptions())
        )

        assertEquals(
            listOf(
                CreateSequence::class,
                AlterColumn::class,
                CreateIndex::class,
                AddForeignKey::class,
                AddComment::class,
                AddComment::class,
            ),
            operations.map { it::class }
        )
    }
}
