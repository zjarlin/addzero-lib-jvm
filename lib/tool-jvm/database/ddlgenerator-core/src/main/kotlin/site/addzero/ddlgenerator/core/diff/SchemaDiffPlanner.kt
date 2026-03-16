package site.addzero.ddlgenerator.core.diff

import site.addzero.ddlgenerator.core.model.AutoDdlColumn
import site.addzero.ddlgenerator.core.model.AutoDdlComment
import site.addzero.ddlgenerator.core.model.AutoDdlCommentTargetType
import site.addzero.ddlgenerator.core.model.AutoDdlForeignKey
import site.addzero.ddlgenerator.core.model.AutoDdlIndex
import site.addzero.ddlgenerator.core.model.AutoDdlSchema
import site.addzero.ddlgenerator.core.options.AutoDdlDiffOptions

object SchemaDiffPlanner {

    fun plan(
        desiredSchema: AutoDdlSchema,
        actualSchema: AutoDdlSchema,
        options: AutoDdlDiffOptions = AutoDdlDiffOptions(),
    ): List<AutoDdlOperation> {
        val desired = desiredSchema.filtered(options)
        val actual = actualSchema.filtered(options)
        val operations = mutableListOf<AutoDdlOperation>()

        if (options.includeSequences) {
            val actualSequences = actual.sequences.associateBy { it.name.lowercase() }
            desired.sequences.forEach { sequence ->
                if (actualSequences[sequence.name.lowercase()] == null) {
                    operations += CreateSequence(sequence)
                }
            }
        }

        val desiredTables = desired.tables.associateBy { it.name.lowercase() }
        val actualTables = actual.tables.associateBy { it.name.lowercase() }

        desired.tables.forEach { desiredTable ->
            val actualTable = actualTables[desiredTable.name.lowercase()]
            if (actualTable == null) {
                operations += CreateTable(desiredTable)
                if (options.includeComments) {
                    collectCommentsForTable(desiredTable).forEach { operations += AddComment(it) }
                }
                return@forEach
            }
            operations += diffColumns(desiredTable, actualTable, options)
            if (options.includeIndexes) {
                operations += diffIndexes(desiredTable, actualTable, options)
            }
            if (options.includeForeignKeys) {
                operations += diffForeignKeys(desiredTable, actualTable, options)
            }
            if (options.includeComments) {
                operations += diffComments(desiredTable, actualTable)
            }
        }

        if (options.allowDestructiveChanges) {
            actual.tables
                .filter { desiredTables[it.name.lowercase()] == null }
                .forEach { operations += DropTable(it.name) }
        }

        return operations.sortedWith(compareBy(::operationRank, ::operationTarget))
    }

    private fun diffColumns(
        desiredTable: site.addzero.ddlgenerator.core.model.AutoDdlTable,
        actualTable: site.addzero.ddlgenerator.core.model.AutoDdlTable,
        options: AutoDdlDiffOptions,
    ): List<AutoDdlOperation> {
        val operations = mutableListOf<AutoDdlOperation>()
        val actualColumns = actualTable.columns.associateBy { it.name.lowercase() }
        val desiredColumns = desiredTable.columns.associateBy { it.name.lowercase() }

        desiredTable.columns.forEach { desiredColumn ->
            val actualColumn = actualColumns[desiredColumn.name.lowercase()]
            if (actualColumn == null) {
                operations += AddColumn(desiredTable.name, desiredColumn)
                return@forEach
            }
            if (desiredColumn.isDifferentFrom(actualColumn)) {
                operations += AlterColumn(desiredTable.name, desiredColumn, actualColumn)
            }
        }

        if (options.allowDestructiveChanges) {
            actualTable.columns
                .filter { desiredColumns[it.name.lowercase()] == null }
                .forEach { operations += DropColumn(desiredTable.name, it.name) }
        }

        return operations
    }

    private fun diffIndexes(
        desiredTable: site.addzero.ddlgenerator.core.model.AutoDdlTable,
        actualTable: site.addzero.ddlgenerator.core.model.AutoDdlTable,
        options: AutoDdlDiffOptions,
    ): List<AutoDdlOperation> {
        val operations = mutableListOf<AutoDdlOperation>()
        val actualIndexes = actualTable.indexes
        desiredTable.indexes.forEach { desiredIndex ->
            if (actualIndexes.none { it.matchesIndex(desiredIndex) }) {
                operations += CreateIndex(desiredTable.name, desiredIndex)
            }
        }
        if (options.allowDestructiveChanges) {
            actualIndexes
                .filter { actualIndex -> desiredTable.indexes.none { it.matchesIndex(actualIndex) } }
                .forEach { operations += DropIndex(desiredTable.name, it.name) }
        }
        return operations
    }

    private fun diffForeignKeys(
        desiredTable: site.addzero.ddlgenerator.core.model.AutoDdlTable,
        actualTable: site.addzero.ddlgenerator.core.model.AutoDdlTable,
        options: AutoDdlDiffOptions,
    ): List<AutoDdlOperation> {
        val operations = mutableListOf<AutoDdlOperation>()
        val actualForeignKeys = actualTable.foreignKeys
        desiredTable.foreignKeys.forEach { desiredForeignKey ->
            if (actualForeignKeys.none { it.matchesForeignKey(desiredForeignKey) }) {
                operations += AddForeignKey(desiredTable.name, desiredForeignKey)
            }
        }
        if (options.allowDestructiveChanges) {
            actualForeignKeys
                .filter { actualForeignKey -> desiredTable.foreignKeys.none { it.matchesForeignKey(actualForeignKey) } }
                .forEach { operations += DropForeignKey(desiredTable.name, it.name) }
        }
        return operations
    }

    private fun diffComments(
        desiredTable: site.addzero.ddlgenerator.core.model.AutoDdlTable,
        actualTable: site.addzero.ddlgenerator.core.model.AutoDdlTable,
    ): List<AutoDdlOperation> {
        val operations = mutableListOf<AutoDdlOperation>()
        if (!desiredTable.comment.isNullOrBlank() && desiredTable.comment != actualTable.comment) {
            operations += AddComment(
                AutoDdlComment(
                    targetType = AutoDdlCommentTargetType.TABLE,
                    value = desiredTable.comment,
                    tableName = desiredTable.name,
                )
            )
        }
        desiredTable.columns.forEach { desiredColumn ->
            val actualColumn = actualTable.column(desiredColumn.name)
            val desiredComment = desiredColumn.comment
            if (!desiredComment.isNullOrBlank() && desiredComment != actualColumn?.comment) {
                operations += AddComment(
                    AutoDdlComment(
                        targetType = AutoDdlCommentTargetType.COLUMN,
                        value = desiredComment,
                        tableName = desiredTable.name,
                        columnName = desiredColumn.name,
                    )
                )
            }
        }
        return operations
    }

    private fun collectCommentsForTable(
        table: site.addzero.ddlgenerator.core.model.AutoDdlTable,
    ): List<AutoDdlComment> {
        return buildList {
            if (!table.comment.isNullOrBlank()) {
                add(
                    AutoDdlComment(
                        targetType = AutoDdlCommentTargetType.TABLE,
                        value = table.comment,
                        tableName = table.name,
                    )
                )
            }
            table.columns.forEach { column ->
                if (!column.comment.isNullOrBlank()) {
                    add(
                        AutoDdlComment(
                            targetType = AutoDdlCommentTargetType.COLUMN,
                            value = column.comment,
                            tableName = table.name,
                            columnName = column.name,
                        )
                    )
                }
            }
        }
    }

    private fun AutoDdlSchema.filtered(options: AutoDdlDiffOptions): AutoDdlSchema {
        val filteredTables = tables
            .filterNot { table -> options.excludeTables.matches(table.name) }
            .map { table ->
                table.copy(
                    columns = table.columns.filterNot { column ->
                        options.excludeColumns.matches(column.name) ||
                            options.excludeColumns.matches("${table.name}.${column.name}")
                    }
                )
            }
        return copy(tables = filteredTables)
    }

    private fun List<String>.matches(value: String): Boolean {
        return any { pattern -> value.matchesWildcard(pattern) }
    }

    private fun String.matchesWildcard(pattern: String): Boolean {
        val regex = pattern
            .replace(".", "\\.")
            .replace("*", ".*")
        return Regex("^$regex$", RegexOption.IGNORE_CASE).matches(this)
    }

    private fun AutoDdlColumn.isDifferentFrom(other: AutoDdlColumn): Boolean {
        return logicalType != other.logicalType ||
            nullable != other.nullable ||
            length != other.length ||
            precision != other.precision ||
            scale != other.scale ||
            normalizeDefault(defaultValue) != normalizeDefault(other.defaultValue) ||
            autoIncrement != other.autoIncrement ||
            normalizeSequence(sequenceName) != normalizeSequence(other.sequenceName) ||
            normalizeTypeHint(nativeTypeHint) != normalizeTypeHint(other.nativeTypeHint)
    }

    private fun AutoDdlIndex.matchesIndex(other: AutoDdlIndex): Boolean {
        return name.equals(other.name, ignoreCase = true) ||
            (type == other.type && normalizeNames(columnNames) == normalizeNames(other.columnNames))
    }

    private fun AutoDdlForeignKey.matchesForeignKey(other: AutoDdlForeignKey): Boolean {
        return normalizeNames(columnNames) == normalizeNames(other.columnNames) &&
            referencedTableName.equals(other.referencedTableName, ignoreCase = true) &&
            normalizeNames(referencedColumnNames) == normalizeNames(other.referencedColumnNames) &&
            onDelete.equals(other.onDelete, ignoreCase = true) &&
            onUpdate.equals(other.onUpdate, ignoreCase = true)
    }

    private fun normalizeNames(values: List<String>): List<String> {
        return values.map { it.lowercase() }
    }

    private fun normalizeDefault(value: String?): String? {
        return value?.trim()?.removeSurrounding("'")
    }

    private fun normalizeSequence(value: String?): String? {
        return value?.trim()?.lowercase()
    }

    private fun normalizeTypeHint(value: String?): String? {
        return value?.trim()?.lowercase()
    }

    private fun operationRank(operation: AutoDdlOperation): Int {
        return when (operation) {
            is DropForeignKey -> 10
            is DropIndex -> 20
            is DropColumn -> 30
            is DropTable -> 40
            is CreateSequence -> 50
            is CreateTable -> 60
            is AddColumn -> 70
            is AlterColumn -> 80
            is CreateIndex -> 90
            is AddForeignKey -> 100
            is AddComment -> 110
        }
    }

    private fun operationTarget(operation: AutoDdlOperation): String {
        return when (operation) {
            is CreateSequence -> operation.sequence.name
            is CreateTable -> operation.table.name
            is DropTable -> operation.tableName
            is AddColumn -> "${operation.tableName}.${operation.column.name}"
            is AlterColumn -> "${operation.tableName}.${operation.column.name}"
            is DropColumn -> "${operation.tableName}.${operation.columnName}"
            is CreateIndex -> "${operation.tableName}.${operation.index.name}"
            is DropIndex -> "${operation.tableName}.${operation.indexName}"
            is AddForeignKey -> "${operation.tableName}.${operation.foreignKey.name}"
            is DropForeignKey -> "${operation.tableName}.${operation.foreignKeyName}"
            is AddComment -> listOfNotNull(operation.comment.tableName, operation.comment.columnName, operation.comment.sequenceName).joinToString(".")
        }
    }
}
