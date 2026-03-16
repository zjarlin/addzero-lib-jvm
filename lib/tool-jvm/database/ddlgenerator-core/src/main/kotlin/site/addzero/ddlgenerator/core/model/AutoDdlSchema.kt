package site.addzero.ddlgenerator.core.model

enum class AutoDdlLogicalType {
    STRING,
    TEXT,
    CHAR,
    BOOLEAN,
    INT8,
    INT16,
    INT32,
    INT64,
    DECIMAL,
    BIG_INTEGER,
    FLOAT32,
    FLOAT64,
    DATE,
    TIME,
    DATETIME,
    DATETIME_TZ,
    TIMESTAMP,
    DURATION,
    BINARY,
    UUID,
    JSON,
    UNKNOWN,
}

enum class AutoDdlIndexType {
    NORMAL,
    UNIQUE,
    FULLTEXT,
}

enum class AutoDdlCommentTargetType {
    TABLE,
    COLUMN,
    SEQUENCE,
}

data class AutoDdlComment(
    val targetType: AutoDdlCommentTargetType,
    val value: String,
    val tableName: String? = null,
    val columnName: String? = null,
    val sequenceName: String? = null,
)

data class AutoDdlSequence(
    val name: String,
    val startWith: Long = 1,
    val incrementBy: Int = 1,
    val comment: String? = null,
)

data class AutoDdlIndex(
    val name: String,
    val columnNames: List<String>,
    val type: AutoDdlIndexType = AutoDdlIndexType.NORMAL,
)

data class AutoDdlForeignKey(
    val name: String,
    val columnNames: List<String>,
    val referencedTableName: String,
    val referencedColumnNames: List<String>,
    val onDelete: String? = null,
    val onUpdate: String? = null,
)

data class AutoDdlJunction(
    val leftTableName: String,
    val rightTableName: String,
    val leftColumnName: String,
    val rightColumnName: String,
)

data class AutoDdlColumn(
    val name: String,
    val logicalType: AutoDdlLogicalType,
    val nullable: Boolean = true,
    val length: Int? = null,
    val precision: Int? = null,
    val scale: Int? = null,
    val defaultValue: String? = null,
    val comment: String? = null,
    val primaryKey: Boolean = false,
    val autoIncrement: Boolean = false,
    val sequenceName: String? = null,
    val nativeTypeHint: String? = null,
)

data class AutoDdlTable(
    val name: String,
    val columns: List<AutoDdlColumn>,
    val foreignKeys: List<AutoDdlForeignKey> = emptyList(),
    val indexes: List<AutoDdlIndex> = emptyList(),
    val comment: String? = null,
    val junction: AutoDdlJunction? = null,
) {
    val primaryKeyColumnNames: List<String>
        get() = columns.filter { it.primaryKey }.map { it.name }

    fun column(name: String): AutoDdlColumn? {
        return columns.firstOrNull { it.name.equals(name, ignoreCase = true) }
    }
}

data class AutoDdlSchema(
    val tables: List<AutoDdlTable>,
    val sequences: List<AutoDdlSequence> = emptyList(),
) {
    fun table(name: String): AutoDdlTable? {
        return tables.firstOrNull { it.name.equals(name, ignoreCase = true) }
    }
}
