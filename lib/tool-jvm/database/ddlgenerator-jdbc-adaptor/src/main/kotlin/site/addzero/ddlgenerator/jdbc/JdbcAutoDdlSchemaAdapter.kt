package site.addzero.ddlgenerator.jdbc

import site.addzero.ddlgenerator.core.model.AutoDdlColumn
import site.addzero.ddlgenerator.core.model.AutoDdlForeignKey
import site.addzero.ddlgenerator.core.model.AutoDdlIndex
import site.addzero.ddlgenerator.core.model.AutoDdlIndexType
import site.addzero.ddlgenerator.core.model.AutoDdlLogicalType
import site.addzero.ddlgenerator.core.model.AutoDdlSchema
import site.addzero.ddlgenerator.core.model.AutoDdlTable
import site.addzero.entity.ForeignKeyMetadata
import site.addzero.entity.JdbcColumnMetadata
import site.addzero.entity.JdbcIndexMetadata
import site.addzero.entity.JdbcTableMetadata
import java.sql.Types

object JdbcAutoDdlSchemaAdapter {

    fun from(
        tables: List<JdbcTableMetadata>,
        foreignKeys: List<ForeignKeyMetadata> = emptyList(),
        indexes: List<JdbcIndexMetadata> = emptyList(),
    ): AutoDdlSchema {
        val foreignKeysByTable = foreignKeys.groupBy { it.fkTableName.lowercase() }
        val indexesByTable = indexes.groupBy { it.tableName.lowercase() }
        return AutoDdlSchema(
            tables = tables.map { table ->
                AutoDdlTable(
                    name = table.tableName,
                    comment = table.remarks.takeIf { it.isNotBlank() },
                    columns = table.columns.map { it.toColumn() },
                    foreignKeys = foreignKeysByTable[table.tableName.lowercase()].orEmpty().map { it.toForeignKey() },
                    indexes = indexesByTable[table.tableName.lowercase()].orEmpty().map { it.toIndex() },
                )
            }
        )
    }

    private fun JdbcColumnMetadata.toColumn(): AutoDdlColumn {
        return AutoDdlColumn(
            name = columnName,
            logicalType = toLogicalType(),
            nullable = nullable,
            length = columnLength,
            defaultValue = defaultValue,
            comment = remarks.takeIf { it.isNotBlank() },
            primaryKey = isPrimaryKey,
            nativeTypeHint = columnType,
        )
    }

    private fun JdbcIndexMetadata.toIndex(): AutoDdlIndex {
        return AutoDdlIndex(
            name = name,
            columnNames = columnNames,
            type = if (unique) AutoDdlIndexType.UNIQUE else AutoDdlIndexType.NORMAL,
        )
    }

    private fun ForeignKeyMetadata.toForeignKey(): AutoDdlForeignKey {
        return AutoDdlForeignKey(
            name = fkName ?: "fk_${fkTableName}_${fkColumnName}",
            columnNames = listOf(fkColumnName),
            referencedTableName = pkTableName,
            referencedColumnNames = listOf(pkColumnName),
        )
    }

    private fun JdbcColumnMetadata.toLogicalType(): AutoDdlLogicalType {
        return when (jdbcType) {
            Types.CHAR, Types.NCHAR -> AutoDdlLogicalType.CHAR
            Types.VARCHAR, Types.NVARCHAR, Types.LONGVARCHAR, Types.LONGNVARCHAR -> {
                if ((columnLength ?: 0) > 1000 || columnType.contains("text", ignoreCase = true) || columnType.contains("clob", ignoreCase = true)) {
                    AutoDdlLogicalType.TEXT
                } else {
                    AutoDdlLogicalType.STRING
                }
            }
            Types.CLOB, Types.NCLOB -> AutoDdlLogicalType.TEXT
            Types.BOOLEAN, Types.BIT -> AutoDdlLogicalType.BOOLEAN
            Types.TINYINT -> AutoDdlLogicalType.INT8
            Types.SMALLINT -> AutoDdlLogicalType.INT16
            Types.INTEGER -> AutoDdlLogicalType.INT32
            Types.BIGINT -> AutoDdlLogicalType.INT64
            Types.FLOAT, Types.REAL -> AutoDdlLogicalType.FLOAT32
            Types.DOUBLE -> AutoDdlLogicalType.FLOAT64
            Types.NUMERIC, Types.DECIMAL -> AutoDdlLogicalType.DECIMAL
            Types.DATE -> AutoDdlLogicalType.DATE
            Types.TIME, Types.TIME_WITH_TIMEZONE -> AutoDdlLogicalType.TIME
            Types.TIMESTAMP -> AutoDdlLogicalType.DATETIME
            Types.TIMESTAMP_WITH_TIMEZONE -> AutoDdlLogicalType.DATETIME_TZ
            Types.BINARY, Types.VARBINARY, Types.LONGVARBINARY, Types.BLOB -> AutoDdlLogicalType.BINARY
            Types.OTHER -> when {
                columnType.contains("uuid", ignoreCase = true) -> AutoDdlLogicalType.UUID
                columnType.contains("json", ignoreCase = true) -> AutoDdlLogicalType.JSON
                else -> AutoDdlLogicalType.UNKNOWN
            }
            else -> when {
                columnType.contains("uuid", ignoreCase = true) -> AutoDdlLogicalType.UUID
                columnType.contains("json", ignoreCase = true) -> AutoDdlLogicalType.JSON
                else -> AutoDdlLogicalType.UNKNOWN
            }
        }
    }
}
