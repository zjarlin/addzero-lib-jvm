package site.addzero.ddlgenerator.dialect.postgresql

import kotlin.test.Test
import kotlin.test.assertEquals
import site.addzero.ddlgenerator.core.diff.AddColumn
import site.addzero.ddlgenerator.core.diff.AddPrimaryKey
import site.addzero.ddlgenerator.core.diff.AlterColumn
import site.addzero.ddlgenerator.core.diff.CreateIndex
import site.addzero.ddlgenerator.core.diff.CreateTable
import site.addzero.ddlgenerator.core.diff.DropColumn
import site.addzero.ddlgenerator.core.diff.DropColumnNotNull
import site.addzero.ddlgenerator.core.diff.DropIndex
import site.addzero.ddlgenerator.core.diff.RenameTable
import site.addzero.ddlgenerator.core.diff.SetColumnNotNull
import site.addzero.ddlgenerator.core.model.AutoDdlColumn
import site.addzero.ddlgenerator.core.model.AutoDdlForeignKey
import site.addzero.ddlgenerator.core.model.AutoDdlIndex
import site.addzero.ddlgenerator.core.model.AutoDdlIndexType
import site.addzero.ddlgenerator.core.model.AutoDdlLogicalType
import site.addzero.ddlgenerator.core.model.AutoDdlSchema
import site.addzero.ddlgenerator.core.model.AutoDdlTable

class PostgreSqlAutoDdlDialectTest {

    @Test
    fun `normalizes unbounded strings to text before schema diff`() {
        val dialect = PostgreSqlAutoDdlDialect()
        val schema = AutoDdlSchema(
            tables = listOf(
                AutoDdlTable(
                    name = "book",
                    columns = listOf(
                        AutoDdlColumn("title", AutoDdlLogicalType.STRING),
                        AutoDdlColumn("code", AutoDdlLogicalType.STRING, length = 64),
                    ),
                )
            )
        )

        val normalized = dialect.normalizeSchema(schema)

        assertEquals(AutoDdlLogicalType.TEXT, normalized.table("book")?.column("title")?.logicalType)
        assertEquals(AutoDdlLogicalType.STRING, normalized.table("book")?.column("code")?.logicalType)
        assertEquals(64, normalized.table("book")?.column("code")?.length)

        val previous = dialect.normalizePreviousSchema(schema)
        assertEquals(AutoDdlLogicalType.STRING, previous.table("book")?.column("title")?.logicalType)
        assertEquals(255, previous.table("book")?.column("title")?.length)
    }

    @Test
    fun `normalizes postgresql default numeric facets before schema diff`() {
        val dialect = PostgreSqlAutoDdlDialect()
        val schema = AutoDdlSchema(
            tables = listOf(
                AutoDdlTable(
                    name = "measurement",
                    columns = listOf(
                        AutoDdlColumn("value", AutoDdlLogicalType.DECIMAL),
                        AutoDdlColumn("counter", AutoDdlLogicalType.BIG_INTEGER),
                    ),
                )
            )
        )

        val normalized = dialect.normalizeSchema(schema)

        assertEquals(19, normalized.table("measurement")?.column("value")?.precision)
        assertEquals(2, normalized.table("measurement")?.column("value")?.scale)
        assertEquals(65, normalized.table("measurement")?.column("counter")?.precision)
        assertEquals(0, normalized.table("measurement")?.column("counter")?.scale)
    }

    @Test
    fun `renders string without explicit length as text`() {
        val dialect = PostgreSqlAutoDdlDialect()
        val statements = dialect.render(
            listOf(
                CreateTable(
                    AutoDdlTable(
                        name = "book",
                        columns = listOf(
                            AutoDdlColumn("description", AutoDdlLogicalType.STRING),
                        )
                    )
                )
            )
        )

        assertEquals(
            listOf(
                """
                CREATE TABLE IF NOT EXISTS "book" (
                  "description" TEXT
                );
                """.trimIndent()
            ),
            statements
        )
    }

    @Test
    fun `renders explicit string length change as varchar`() {
        val dialect = PostgreSqlAutoDdlDialect()
        val statements = dialect.render(
            listOf(
                AlterColumn(
                    tableName = "book",
                    column = AutoDdlColumn("title", AutoDdlLogicalType.STRING, length = 128),
                    previousColumn = AutoDdlColumn("title", AutoDdlLogicalType.TEXT),
                )
            )
        )

        assertEquals(
            listOf(
                """ALTER TABLE "book" ALTER COLUMN "title" DROP DEFAULT;""",
                """
                ALTER TABLE "book"
                  ALTER COLUMN "title" TYPE VARCHAR(128)
                  USING "title"::VARCHAR(128);
                """.trimIndent(),
            ),
            statements,
        )
    }

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
                CREATE TABLE IF NOT EXISTS "book" (
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
    fun `renders add required column with backfill steps`() {
        val dialect = PostgreSqlAutoDdlDialect()
        val statements = dialect.render(
            listOf(
                AddColumn(
                    tableName = "book",
                    column = AutoDdlColumn("title", AutoDdlLogicalType.STRING, nullable = false, length = 128)
                )
            )
        )

        assertEquals(
            listOf(
                """ALTER TABLE "book" ADD COLUMN IF NOT EXISTS "title" VARCHAR(128) DEFAULT '';""",
                """UPDATE "book" SET "title" = '' WHERE "title" IS NULL;""",
                """ALTER TABLE "book" ALTER COLUMN "title" SET NOT NULL;""",
                """ALTER TABLE "book" ALTER COLUMN "title" DROP DEFAULT;""",
            ),
            statements
        )
    }

    @Test
    fun `renders unbounded primary key text column backfill before idempotent primary key creation`() {
        val dialect = PostgreSqlAutoDdlDialect()
        val statements = dialect.render(
            listOf(
                AddColumn(
                    tableName = "biz_mapping",
                    column = AutoDdlColumn(
                        "mapping_type",
                        AutoDdlLogicalType.STRING,
                        nullable = false,
                        primaryKey = true,
                    )
                ),
                AddPrimaryKey(
                    tableName = "biz_mapping",
                    columnNames = listOf("from_id", "to_id", "mapping_type"),
                )
            )
        )

        assertEquals(
            listOf(
                """ALTER TABLE "biz_mapping" ADD COLUMN IF NOT EXISTS "mapping_type" TEXT DEFAULT '';""",
                """UPDATE "biz_mapping" SET "mapping_type" = '' WHERE "mapping_type" IS NULL;""",
                """ALTER TABLE "biz_mapping" ALTER COLUMN "mapping_type" SET NOT NULL;""",
                """ALTER TABLE "biz_mapping" ALTER COLUMN "mapping_type" DROP DEFAULT;""",
                """
                DO ${'$'}${'$'}
                BEGIN
                  IF to_regclass('biz_mapping') IS NOT NULL
                     AND NOT EXISTS (
                       SELECT 1
                       FROM pg_constraint
                       WHERE conrelid = to_regclass('biz_mapping')
                         AND contype = 'p'
                     ) THEN
                    ALTER TABLE "biz_mapping" ADD PRIMARY KEY ("from_id", "to_id", "mapping_type");
                  END IF;
                END ${'$'}${'$'};
                """.trimIndent(),
            ),
            statements,
        )
    }

    @Test
    fun `renders guarded rename table`() {
        val dialect = PostgreSqlAutoDdlDialect()
        val statements = dialect.render(
            listOf(RenameTable(oldTableName = "biz_user", newTableName = "biz_user_ext"))
        )

        assertEquals(1, statements.size)
        assertEquals(
            """
            DO $$
            BEGIN
              IF to_regclass('biz_user') IS NOT NULL
                 AND to_regclass('biz_user_ext') IS NULL THEN
                ALTER TABLE "biz_user" RENAME TO "biz_user_ext";
              END IF;
            END $$;
            """.trimIndent(),
            statements.single()
        )
    }

    @Test
    fun `renders nullable relax operation`() {
        val dialect = PostgreSqlAutoDdlDialect()
        val statements = dialect.render(
            listOf(DropColumnNotNull(tableName = "book", columnName = "subtitle"))
        )

        assertEquals(
            listOf("""ALTER TABLE "book" ALTER COLUMN "subtitle" DROP NOT NULL;"""),
            statements
        )
    }

    @Test
    fun `renders drop index idempotently`() {
        val dialect = PostgreSqlAutoDdlDialect()
        val statements = dialect.render(
            listOf(DropIndex(tableName = "system_area", indexName = "uk_system_area_name"))
        )

        assertEquals(
            listOf("""DROP INDEX IF EXISTS "uk_system_area_name";"""),
            statements
        )
    }

    @Test
    fun `renders drop column idempotently`() {
        val dialect = PostgreSqlAutoDdlDialect()
        val statements = dialect.render(
            listOf(DropColumn(tableName = "iot_malfunction", columnName = "repair_task"))
        )

        assertEquals(
            listOf("""ALTER TABLE "iot_malfunction" DROP COLUMN IF EXISTS "repair_task";"""),
            statements
        )
    }

    @Test
    fun `renders nullable tighten operation with backfill`() {
        val dialect = PostgreSqlAutoDdlDialect()
        val statements = dialect.render(
            listOf(
                SetColumnNotNull(
                    tableName = "book",
                    column = AutoDdlColumn("title", AutoDdlLogicalType.STRING, nullable = false, length = 128)
                )
            )
        )

        assertEquals(
            listOf(
                """UPDATE "book" SET "title" = '' WHERE "title" IS NULL;""",
                """ALTER TABLE "book" ALTER COLUMN "title" SET NOT NULL;""",
            ),
            statements
        )
    }

    @Test
    fun `renders nullable string unique index ignoring blank values`() {
        val dialect = PostgreSqlAutoDdlDialect()
        val statements = dialect.render(
            listOf(
                CreateIndex(
                    tableName = "system_users",
                    index = AutoDdlIndex(
                        name = "uk_system_users_email",
                        columnNames = listOf("email"),
                        type = AutoDdlIndexType.UNIQUE,
                        ignoreBlankValues = true,
                    )
                )
            )
        )

        assertEquals(
            listOf(
                """CREATE UNIQUE INDEX IF NOT EXISTS "uk_system_users_email" ON "system_users" ("email") WHERE "email" IS NOT NULL AND "email" <> '';"""
            ),
            statements
        )
    }

    @Test
    fun `renders json column with native type hint`() {
        val dialect = PostgreSqlAutoDdlDialect()
        val statements = dialect.render(
            listOf(
                CreateTable(
                    AutoDdlTable(
                        name = "iot_thing_model",
                        columns = listOf(
                            AutoDdlColumn(
                                name = "property_spec",
                                logicalType = AutoDdlLogicalType.JSON,
                                nativeTypeHint = "TEXT",
                            ),
                        ),
                    )
                )
            )
        )

        assertEquals(
            listOf(
                """
                CREATE TABLE IF NOT EXISTS "iot_thing_model" (
                  "property_spec" TEXT
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
                """ALTER TABLE "book" ALTER COLUMN "title" DROP DEFAULT;""",
                """
                ALTER TABLE "book"
                  ALTER COLUMN "title" TYPE VARCHAR(128)
                  USING "title"::VARCHAR(128);
                """.trimIndent(),
                """ALTER TABLE "book" ALTER COLUMN "title" SET NOT NULL;""",
                """ALTER TABLE "book" ALTER COLUMN "title" SET DEFAULT 'N/A';""",
            ),
            statements
        )
    }

    @Test
    fun `renders string to bigint alter column with safe cast and default drop`() {
        val dialect = PostgreSqlAutoDdlDialect()
        val statements = dialect.render(
            listOf(
                AlterColumn(
                    tableName = "iot_plugin_config",
                    column = AutoDdlColumn("creator", AutoDdlLogicalType.INT64),
                    previousColumn = AutoDdlColumn("creator", AutoDdlLogicalType.STRING, defaultValue = "''")
                )
            )
        )

        assertEquals(
            listOf(
                """ALTER TABLE "iot_plugin_config" ALTER COLUMN "creator" DROP DEFAULT;""",
                """
                ALTER TABLE "iot_plugin_config"
                  ALTER COLUMN "creator" TYPE BIGINT
                  USING CASE
                  WHEN "creator" IS NULL OR btrim("creator"::text) = '' THEN NULL
                  WHEN btrim("creator"::text) ~ '^[+-]?[0-9]+$' THEN btrim("creator"::text)::BIGINT
                  ELSE NULL
                END;
                """.trimIndent(),
            ),
            statements
        )
    }
}
