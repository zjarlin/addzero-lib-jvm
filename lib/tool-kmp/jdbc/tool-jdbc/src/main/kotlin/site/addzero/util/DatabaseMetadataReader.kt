package site.addzero.util

import site.addzero.entity.ForeignKeyMetadata
import site.addzero.entity.JdbcColumnMetadata
import site.addzero.entity.JdbcTableMetadata
import site.addzero.entity.PrimaryKeyMetadata
import site.addzero.util.db.SqlExecutor
import java.security.MessageDigest
import java.sql.DatabaseMetaData

/**
 * 数据库元数据读取器（内部使用）
 * 用于获取数据库的表、列、主键、外键等元数据信息
 */
class DatabaseMetadataReader(
    private val url: String,
    private val username: String,
    private val password: String
) {
    private val sqlExecutor = SqlExecutor(url, username, password)

    /**
     * 通配符匹配（支持 *）
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
     */
    private fun shouldIncludeTable(
        tableName: String,
        includeRules: List<String>?,
        excludeRules: List<String>?
    ): Boolean {
        // 1. 如果指定了包含列表，则检查是否匹配任一包含规则
        includeRules?.let { rules ->
            if (rules.isNotEmpty()) {
                return rules.any { rule -> matchesWildcard(tableName, rule) }
            }
        }

        // 2. 如果指定了排除列表，则检查是否匹配任一排除规则
        excludeRules?.let { rules ->
            if (rules.isNotEmpty()) {
                return !rules.any { rule -> matchesWildcard(tableName, rule) }
            }
        }

        // 3. 默认包含所有表
        return true
    }

    /**
     * 获取数据库连接并执行操作
     */
    private fun <T> withConnection(block: (java.sql.Connection) -> T): T {
        return try {
            val connection = java.sql.DriverManager.getConnection(url, username, password)
            connection.use(block)
        } catch (e: Exception) {
            throw RuntimeException("Failed to connect to database", e)
        }
    }

    /**
     * 获取所有表的元数据
     */
    fun getTableMetaData(
        schema: String = "public",
        includeRules: List<String>? = null,
        excludeRules: List<String>? = null
    ): List<JdbcTableMetadata> {
        return withConnection { connection ->
            val metaData = connection.metaData
            val tables = mutableListOf<JdbcTableMetadata>()

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
                    val columns = getColumnsMetadata(schema, tableName)

                    // 获取表的主键信息
                    val primaryKeys = getPrimaryKeysForTable(tableName, schema)

                    // 标记主键列
                    val columnsWithPk = columns.map { column ->
                        column.copy(isPrimaryKey = primaryKeys.contains(column.columnName))
                    }

                    // 创建表元数据
                    val tableMetadata = JdbcTableMetadata(
                        tableName = tableName,
                        schema = schema,
                        tableType = tableType,
                        remarks = remarks,
                        columns = columnsWithPk
                    )

                    tables.add(tableMetadata)
                }
            }
            tables
        }
    }

    /**
     * 获取所有主键元数据
     */
    fun getPrimaryKeysMetadata(schema: String = "public"): List<PrimaryKeyMetadata> {
        return withConnection { connection ->
            getPrimaryKeysMetadataInternal(connection, schema)
                .sortedWith(compareBy({ it.tableName }, { it.keySeq }))
        }
    }

    private fun getPrimaryKeysMetadataInternal(
        connection: java.sql.Connection,
        schema: String
    ): List<PrimaryKeyMetadata> {
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

    /**
     * 获取所有外键元数据
     */
    fun getForeignKeysMetadata(schema: String = "public"): List<ForeignKeyMetadata> {
        return withConnection { connection ->
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

            result.sortedWith(compareBy({ it.pkTableName }, { it.fkTableName }, { it.keySeq }))
        }
    }

    /**
     * 获取列元数据
     */
    fun getColumnsMetadata(
        schema: String = "public",
        tableName: String? = null
    ): List<JdbcColumnMetadata> {
        return withConnection { connection ->
            getColumnsMetadataInternal(connection, schema, tableName)
        }
    }

    private fun getColumnsMetadataInternal(
        connection: java.sql.Connection,
        schema: String,
        tableName: String?
    ): List<JdbcColumnMetadata> {
        val metadata = connection.metaData
        val columns = metadata.getColumns(null, schema, tableName, "%")
        val result = mutableListOf<JdbcColumnMetadata>()

        while (columns.next()) {
            val defaultValue = columns.getString("COLUMN_DEF")
            val remarks = columns.getString("REMARKS") ?: ""
            val jdbcType = columns.getInt("DATA_TYPE")
            val currentTableName = columns.getString("TABLE_NAME")
            val columnName = columns.getString("COLUMN_NAME")
            val dataType = columns.getString("TYPE_NAME")
            val columnSize = columns.getInt("COLUMN_SIZE")
            val nullable = columns.getInt("NULLABLE")

            // 生成MD5哈希ID
            val id = generateMd5("$currentTableName.$columnName")

            // 转换数据类型
            val normalizedType = normalizeDataType(dataType)

            // 转换可空标志
            val nullableBool = nullable != DatabaseMetaData.columnNullable
            val nullableFlag = if (nullableBool) "NO" else "YES"

            val element = JdbcColumnMetadata(
                tableName = currentTableName,
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
            result.add(element)
        }

        // 按表名和列的位置排序
        return result.sortedWith(
            compareBy({ it.tableName }, { getColumnPosition(connection, schema, it.tableName, it.columnName) })
        )
    }

    /**
     * 获取表的主键列
     */
    private fun getPrimaryKeysForTable(tableName: String, schema: String): Set<String> {
        return withConnection { connection ->
            val metaData = connection.metaData
            val primaryKeys = mutableSetOf<String>()
            val pkResultSet = metaData.getPrimaryKeys(null, schema, tableName)

            while (pkResultSet.next()) {
                val columnName = pkResultSet.getString("COLUMN_NAME")
                primaryKeys.add(columnName)
            }

            primaryKeys
        }
    }

    /**
     * 获取列的位置
     */
    private fun getColumnPosition(
        connection: java.sql.Connection,
        schema: String,
        tableName: String,
        columnName: String
    ): Int {
        return try {
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
                        rs.getInt(1)
                    } else {
                        Int.MAX_VALUE
                    }
                }
            }
        } catch (e: Exception) {
            Int.MAX_VALUE
        }
    }

    /**
     * 标准化数据类型名称
     */
    private fun normalizeDataType(dataType: String): String {
        return when (dataType.lowercase()) {
            "character varying" -> "varchar"
            "varchar" -> "varchar"
            "character" -> "char"
            else -> dataType.lowercase()
        }
    }

    /**
     * 生成MD5哈希
     */
    private fun generateMd5(input: String): String {
        val md = MessageDigest.getInstance("MD5")
        val digest = md.digest(input.toByteArray())
        return digest.joinToString("") { "%02x".format(it) }
    }

    /**
     * 关闭资源
     */
    fun close() {
        sqlExecutor.close()
    }
}

/**
 * 便捷的伴生对象，提供静态方法
 */
