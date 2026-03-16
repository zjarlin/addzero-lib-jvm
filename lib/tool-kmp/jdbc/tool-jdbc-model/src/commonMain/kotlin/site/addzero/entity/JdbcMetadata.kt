package site.addzero.entity

/**
 * 列元数据数据类
 */
data class JdbcColumnMetadata(
    val tableName: String,
    val columnName: String,
    val jdbcType: Int,
    val columnType: String,
    val columnLength: Int?,
    val nullable: Boolean,
    val nullableFlag: String,
    val remarks: String,
    val defaultValue: String?,
    var isPrimaryKey: Boolean
)

/**
 * 表元数据数据类
 */
data class JdbcTableMetadata(
    val tableName: String,
    val schema: String,
    val tableType: String,
    val remarks: String,
    val columns: List<JdbcColumnMetadata>
)

data class JdbcIndexMetadata(
    val tableName: String,
    val name: String,
    val columnNames: List<String>,
    val unique: Boolean
)
