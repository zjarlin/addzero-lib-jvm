package site.addzero.ddlgenerator.runtime

import site.addzero.ddlgenerator.core.diff.AddComment
import site.addzero.ddlgenerator.core.diff.AddForeignKey
import site.addzero.ddlgenerator.core.diff.AutoDdlOperation
import site.addzero.ddlgenerator.core.diff.CreateIndex
import site.addzero.ddlgenerator.core.diff.CreateSequence
import site.addzero.ddlgenerator.core.diff.CreateTable
import site.addzero.ddlgenerator.core.diff.SchemaDiffPlanner
import site.addzero.ddlgenerator.core.dialect.AutoDdlDialects
import site.addzero.ddlgenerator.core.model.AutoDdlComment
import site.addzero.ddlgenerator.core.model.AutoDdlCommentTargetType
import site.addzero.ddlgenerator.core.model.AutoDdlSchema
import site.addzero.ddlgenerator.core.options.AutoDdlDiffOptions
import site.addzero.ddlgenerator.core.options.AutoDdlOptions
import site.addzero.ddlgenerator.jdbc.JdbcAutoDdlSchemaAdapter
import site.addzero.ddlgenerator.lsi.LsiAutoDdlSchemaAdapter
import site.addzero.lsi.clazz.LsiClass
import site.addzero.util.DatabaseMetadataReader
import site.addzero.util.db.DatabaseType

data class AutoDdlJdbcConfig(
    val jdbcUrl: String,
    val jdbcUsername: String = "",
    val jdbcPassword: String = "",
    val schema: String? = null,
    val excludeTables: List<String> = emptyList(),
)

data class AutoDdlDiffResult(
    val desiredSchema: AutoDdlSchema,
    val actualSchema: AutoDdlSchema,
    val operations: List<AutoDdlOperation>,
    val statements: List<String>,
) {
    fun toSql(): String {
        return statements.joinToString("\n")
    }
}

object AutoDdlRuntime {

    fun desiredSchema(
        lsiClasses: List<LsiClass>,
        includeManyToManyTables: Boolean = true,
    ): AutoDdlSchema {
        val baseSchema = LsiAutoDdlSchemaAdapter.from(lsiClasses)
        if (!includeManyToManyTables) {
            return baseSchema
        }
        val junctionTables = LsiAutoDdlSchemaAdapter.scanManyToManyTables(lsiClasses)
        return baseSchema.copy(
            tables = (baseSchema.tables + junctionTables).distinctBy { it.name.lowercase() }
        )
    }

    fun generate(
        lsiClasses: List<LsiClass>,
        databaseType: DatabaseType,
        options: AutoDdlOptions = AutoDdlOptions(),
        includeManyToManyTables: Boolean = true,
    ): List<String> {
        val schema = desiredSchema(lsiClasses, includeManyToManyTables)
        val operations = generateOperations(schema, options)
        return AutoDdlDialects.require(databaseType).render(operations)
    }

    fun diff(
        lsiClasses: List<LsiClass>,
        actualSchema: AutoDdlSchema,
        databaseType: DatabaseType,
        options: AutoDdlDiffOptions = AutoDdlDiffOptions(),
        includeManyToManyTables: Boolean = true,
    ): AutoDdlDiffResult {
        val desired = desiredSchema(lsiClasses, includeManyToManyTables)
        val operations = SchemaDiffPlanner.plan(desired, actualSchema, options)
        val statements = AutoDdlDialects.require(databaseType).render(operations)
        return AutoDdlDiffResult(
            desiredSchema = desired,
            actualSchema = actualSchema,
            operations = operations,
            statements = statements,
        )
    }

    fun diff(
        lsiClasses: List<LsiClass>,
        jdbcConfig: AutoDdlJdbcConfig,
        databaseType: DatabaseType,
        options: AutoDdlDiffOptions = AutoDdlDiffOptions(),
        includeManyToManyTables: Boolean = true,
    ): AutoDdlDiffResult {
        val actualSchema = readActualSchema(jdbcConfig)
        return diff(lsiClasses, actualSchema, databaseType, options, includeManyToManyTables)
    }

    fun readActualSchema(jdbcConfig: AutoDdlJdbcConfig): AutoDdlSchema {
        val reader = DatabaseMetadataReader(
            url = jdbcConfig.jdbcUrl,
            username = jdbcConfig.jdbcUsername,
            password = jdbcConfig.jdbcPassword,
        )
        val tables = reader.getTableMetaData(
            schema = jdbcConfig.schema,
            excludeRules = jdbcConfig.excludeTables.takeIf { it.isNotEmpty() },
        )
        val foreignKeys = reader.getForeignKeysMetadata(schema = jdbcConfig.schema)
        val indexes = reader.getIndexMetadata(schema = jdbcConfig.schema)
        return JdbcAutoDdlSchemaAdapter.from(tables, foreignKeys, indexes)
    }

    private fun generateOperations(
        schema: AutoDdlSchema,
        options: AutoDdlOptions,
    ): List<AutoDdlOperation> {
        return buildList {
            if (options.includeSequences) {
                schema.sequences.forEach { sequence ->
                    add(CreateSequence(sequence))
                }
            }

            schema.tables.forEach { table ->
                add(CreateTable(table))
            }

            if (options.includeIndexes) {
                schema.tables.forEach { table ->
                    table.indexes.forEach { index ->
                        add(CreateIndex(table.name, index))
                    }
                }
            }

            if (options.includeForeignKeys) {
                schema.tables.forEach { table ->
                    table.foreignKeys.forEach { foreignKey ->
                        add(AddForeignKey(table.name, foreignKey))
                    }
                }
            }

            if (options.includeComments) {
                schema.tables.forEach { table ->
                    val tableComment = table.comment
                    if (!tableComment.isNullOrBlank()) {
                        add(
                            AddComment(
                                AutoDdlComment(
                                    targetType = AutoDdlCommentTargetType.TABLE,
                                    value = tableComment,
                                    tableName = table.name,
                                )
                            )
                        )
                    }
                    table.columns
                        .filter { !it.comment.isNullOrBlank() }
                        .forEach { column ->
                            add(
                                AddComment(
                                    AutoDdlComment(
                                        targetType = AutoDdlCommentTargetType.COLUMN,
                                        value = column.comment.orEmpty(),
                                        tableName = table.name,
                                        columnName = column.name,
                                    )
                                )
                            )
                        }
                }
            }
        }
    }
}
