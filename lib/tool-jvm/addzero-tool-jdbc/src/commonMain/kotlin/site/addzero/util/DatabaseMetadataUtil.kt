package site.addzero.util

import site.addzero.entity.ForeignKeyMetadata
import site.addzero.entity.JdbcColumnMetadata
import site.addzero.entity.JdbcTableMetadata
import site.addzero.entity.PrimaryKeyMetadata
import java.security.MessageDigest
import java.sql.Connection
import java.sql.DatabaseMetaData
import kotlin.collections.sortedWith
import kotlin.text.format
import kotlin.use

object DatabaseMetadataUtil {

    /**
     * 通配符匹配（支持 *）
     * @param input 待匹配的字符串（如表名）
     * @param pattern 通配符规则（如 "user_*", "*_mapping"）
     */
    private fun matchesWildcard(input: String, pattern: String): Boolean {
        val regex = pattern
            .replace(".", "\\.")  // 转义点号
            .replace("*", ".*")   // 将 * 转换为正则的 .*
            .let { "^$it$" }      // 完全匹配

        return Regex(regex, RegexOption.IGNORE_CASE).matches(input)
    }

    /**
     * 判断是否应该包含某个表（支持通配符 *）
     * @param tableName 当前表名
     * @param config 数据源配置
     * @return 如果表名符合包含/排除规则则返回 true
     */
    private fun shouldIncludeTable(tableName: String, includeRules: List<String>?, excludeRules: List<String>?):
            Boolean {
        // 1. 如果指定了包含列表，则检查是否匹配任一包含规则
        includeRules?.let { includeRules ->
            if (includeRules.isNotEmpty()) {
                return includeRules.any { rule -> matchesWildcard(tableName, rule) }
            }
        }

        // 2. 如果指定了排除列表，则检查是否匹配任一排除规则
        excludeRules?.let { excludeRules ->
            if (excludeRules.isNotEmpty()) {
                return !excludeRules.any { rule -> matchesWildcard(tableName, rule) }
            }
        }

        // 3. 默认包含所有表
        return true
    }

    fun getTableMetaData(
        connection: Connection,
        schema: String, includeRules: List<String>?, excludeRules: List<String>?
    ): MutableList<JdbcTableMetadata> {
        val tables = mutableListOf<JdbcTableMetadata>()

        val metaData = connection.metaData

        // 获取所有表
        val tablesResultSet = metaData.getTables(
            connection.catalog,
            schema,
            "%",
            arrayOf("TABLE")
        )

        while (tablesResultSet.next()) {
            val tableName = tablesResultSet.getString("TABLE_NAME")

            // 应用表过滤
            if (shouldIncludeTable(tableName, includeRules, excludeRules)) {
                val tableType = tablesResultSet.getString("TABLE_TYPE")
                val remarks = tablesResultSet.getString("REMARKS") ?: ""

                // 获取表的列信息
                val columns = getColumnsMetadata(schema, connection, tableName)

                //                    val columns = getColumnsForTable(metaData, tableName, config)

                // 获取表的主键信息
                val primaryKeys = getPrimaryKeysForTable(connection, tableName, schema = schema)

                // 标记主键列
                columns.forEach { column ->
                    column.isPrimaryKey = primaryKeys.contains(column.columnName)
                }

                // 创建表元数据
                val tableMetadata = JdbcTableMetadata(
                    tableName = tableName,
                    schema = schema,
                    tableType = tableType,
                    remarks = remarks,
                    columns = columns
                )

                tables.add(tableMetadata)
            }
        }
        return tables
    }


    fun getPrimaryKeysMetadata(connection: Connection, schema: String): List<PrimaryKeyMetadata> {
        val result = getprimaryKeysMetadata(connection, schema)

        val sortedWith = result.sortedWith(compareBy({ it.tableName }, { it.keySeq }))
        return sortedWith
    }

    fun getprimaryKeysMetadata(connection: Connection, schema: String): MutableList<PrimaryKeyMetadata> {
        val metadata = connection.metaData
        val primaryKeys = metadata.getPrimaryKeys(null, schema, null)
        val result = mutableListOf<PrimaryKeyMetadata>()

        while (primaryKeys.next()) {
            result.add(
                PrimaryKeyMetadata(
                    tableName = primaryKeys.getString("TABLE_NAME"),
                    columnName = primaryKeys.getString("COLUMN_NAME"),
                    keySeq = primaryKeys.getShort("KEY_SEQ"),
                    pkName = primaryKeys.getString("PK_NAME")
                )
            )
        }
        return result
    }

    fun getForeignKeysMetadata(schema: String = "public", connection: Connection): List<ForeignKeyMetadata> {
        val metadata = connection.metaData
        val foreignKeys = metadata.getExportedKeys(null, schema, null)
        val result = mutableListOf<ForeignKeyMetadata>()

        while (foreignKeys.next()) {
            result.add(
                ForeignKeyMetadata(
                    pkTableName = foreignKeys.getString("PKTABLE_NAME"),
                    pkColumnName = foreignKeys.getString("PKCOLUMN_NAME"),
                    fkTableName = foreignKeys.getString("FKTABLE_NAME"),
                    fkColumnName = foreignKeys.getString("FKCOLUMN_NAME"),
                    keySeq = foreignKeys.getShort("KEY_SEQ"),
                    fkName = foreignKeys.getString("FK_NAME"),
                    pkName = foreignKeys.getString("PK_NAME")
                )
            )
        }

        val sortedWith = result.sortedWith(compareBy({ it.pkTableName }, { it.fkTableName }, { it.keySeq }))
        return sortedWith

    }

    fun getColumnsMetadata(
        schema: String = "public", connection: Connection, tableName: String? = null
    ): List<JdbcColumnMetadata> {
        val metadata = connection.metaData
        val columns = metadata.getColumns(null, schema, tableName, "%")
        val result = mutableListOf<JdbcColumnMetadata>()

        while (columns.next()) {
            val defaultValue = columns.getString("COLUMN_DEF")

            val remarks = columns.getString("REMARKS") ?: ""
            val jdbcType = columns.getInt("DATA_TYPE")
            val tableName = columns.getString("TABLE_NAME")
            val columnName = columns.getString("COLUMN_NAME")
            val dataType = columns.getString("TYPE_NAME")
            val columnSize = columns.getInt("COLUMN_SIZE")
            val nullable = columns.getInt("NULLABLE")

            // 生成MD5哈希ID
            val id = generateMd5("$tableName.$columnName")

            // 转换数据类型
            val normalizedType = normalizeDataType(dataType)

            // 转换可空标志
            val nullableBool = nullable == DatabaseMetaData.columnNoNulls
            val nullableFlag = if (nullableBool) "NO" else "YES"
            val element = JdbcColumnMetadata(
                tableName = tableName,
                columnName = columnName,
                jdbcType = jdbcType,
                columnType = normalizedType,
                columnLength = if (normalizedType in listOf("varchar", "char")) columnSize else null,
                nullable = nullableBool,
                nullableFlag = nullableFlag,
                remarks = remarks,
                defaultValue = defaultValue,
                isPrimaryKey = false  // 稍后会更新此字段
            )
            result.add(
                element
            )


        }


        // 按表名和列的位置排序
        val sortedWith = result.sortedWith(
            compareBy({ it.tableName }, { getColumnPosition(connection, schema, it.tableName, it.columnName) })
        )




        return sortedWith
    }

    private fun normalizeDataType(dataType: String): String {
        return when (dataType.lowercase()) {
            "character varying" -> "varchar"
            "varchar" -> "varchar"
            "character" -> "char"
            else -> dataType.lowercase()
        }
    }

    private fun generateMd5(input: String): String {
        val md = MessageDigest.getInstance("MD5")
        val digest = md.digest(input.toByteArray())
        return digest.joinToString("") { "%02x".format(it) }
    }

    /**
     * 获取表的主键列
     */
    fun getPrimaryKeysForTable(connection: Connection, tableName: String, schema: String): Set<String> {
        val metaData = connection.metaData
        val primaryKeys = mutableSetOf<String>()
        val pkResultSet = metaData.getPrimaryKeys(null, schema, tableName)

        while (pkResultSet.next()) {
            val columnName = pkResultSet.getString("COLUMN_NAME")
            primaryKeys.add(columnName)
        }

        return primaryKeys
    }

    private fun getColumnPosition(connection: Connection, schema: String, tableName: String, columnName: String): Int {
        connection.createStatement().use { statement ->
            val query = """
                SELECT ordinal_position 
                FROM information_schema.columns 
                WHERE table_schema = '$schema' 
                AND table_name = '$tableName' 
                AND column_name = '$columnName'
            """.trimIndent()

            statement.executeQuery(query).use { rs ->
                if (rs.next()) {
                    return rs.getInt(1)
                }
            }
        }
        return Int.MAX_VALUE
    }
}
