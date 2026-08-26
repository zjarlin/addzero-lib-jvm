package site.addzero.ddlgenerator.core.diff

import site.addzero.ddlgenerator.core.model.AutoDdlColumn
import site.addzero.ddlgenerator.core.model.AutoDdlComment
import site.addzero.ddlgenerator.core.model.AutoDdlForeignKey
import site.addzero.ddlgenerator.core.model.AutoDdlIndex
import site.addzero.ddlgenerator.core.model.AutoDdlSequence
import site.addzero.ddlgenerator.core.model.AutoDdlTable

sealed interface AutoDdlOperation {
    val tableName: String?
        get() = null
}

data class CreateSequence(
    val sequence: AutoDdlSequence,
) : AutoDdlOperation

data class CreateTable(
    val table: AutoDdlTable,
) : AutoDdlOperation {
    override val tableName = table.name
}

data class DropTable(
    override val tableName: String,
) : AutoDdlOperation

data class RenameTable(
    val oldTableName: String,
    val newTableName: String,
) : AutoDdlOperation {
    override val tableName = newTableName
}

data class AddColumn(
    override val tableName: String,
    val column: AutoDdlColumn,
) : AutoDdlOperation

data class AlterColumn(
    override val tableName: String,
    val column: AutoDdlColumn,
    val previousColumn: AutoDdlColumn? = null,
) : AutoDdlOperation

data class DropColumnNotNull(
    override val tableName: String,
    val columnName: String,
) : AutoDdlOperation

data class SetColumnNotNull(
    override val tableName: String,
    val column: AutoDdlColumn,
) : AutoDdlOperation

data class DropColumn(
    override val tableName: String,
    val columnName: String,
) : AutoDdlOperation

data class AddPrimaryKey(
    override val tableName: String,
    val columnNames: List<String>,
) : AutoDdlOperation

data class DropPrimaryKey(
    override val tableName: String,
) : AutoDdlOperation

data class CreateIndex(
    override val tableName: String,
    val index: AutoDdlIndex,
) : AutoDdlOperation

data class DropIndex(
    override val tableName: String,
    val indexName: String,
) : AutoDdlOperation

data class AddForeignKey(
    override val tableName: String,
    val foreignKey: AutoDdlForeignKey,
) : AutoDdlOperation

data class DropForeignKey(
    override val tableName: String,
    val foreignKeyName: String,
) : AutoDdlOperation

data class AddComment(
    val comment: AutoDdlComment,
) : AutoDdlOperation {
    override val tableName = comment.tableName
}
