package site.addzero.util.db

import java.sql.Connection
import java.sql.DriverManager
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException

/**
 * 轻量级SQL执行器工具类
 * 支持多种数据库方言，不依赖任何框架
 */
class SqlExecutor(
    private val url: String,
    private val username: String = "",
    private val password: String = "",
    private val driver: String? = null,
) : AutoCloseable {
    private var connection: Connection? = null

    init {
        registerDriver()
    }

    /**
     * 获取数据库连接
     */
    @Throws(SQLException::class)
    private fun getConnection(): Connection {
        if (connection == null || connection!!.isClosed) {
            connection = DriverManager.getConnection(url, username, password)
        }
        return connection!!
    }

    /**
     * 对外暴露连接作用域，统一复用连接生命周期。
     */
    fun <T> withConnection(
        block: (Connection) -> T,
    ): T {
        return block(getConnection())
    }

    /**
     * 在单连接事务中执行逻辑。
     */
    fun <T> withTransaction(
        block: (Connection) -> T,
    ): T {
        val conn = getConnection()
        val originalAutoCommit = conn.autoCommit
        conn.autoCommit = false
        return try {
            val result = block(conn)
            conn.commit()
            result
        } catch (error: Throwable) {
            conn.rollback()
            throw error
        } finally {
            conn.autoCommit = originalAutoCommit
        }
    }

    /**
     * 执行查询SQL并返回结果列表
     *
     * @param sql 要执行的SQL语句
     * @return 查询结果列表，每个元素是一个包含列名和值的Map
     */
    @Throws(SQLException::class)
    fun queryForList(
        sql: String,
        params: List<Any?> = emptyList(),
    ): List<Map<String, Any?>> {
        return query(sql, params) { resultSet ->
            resultSet.toRowMap()
        }
    }

    /**
     * 执行参数化查询。
     */
    @Throws(SQLException::class)
    fun <T> query(
        sql: String,
        params: List<Any?> = emptyList(),
        mapper: (ResultSet) -> T,
    ): List<T> {
        val conn = getConnection()
        prepareStatement(conn, sql, params).use { statement ->
            statement.executeQuery().use { resultSet ->
                val result = mutableListOf<T>()
                while (resultSet.next()) {
                    result += mapper(resultSet)
                }
                return result
            }
        }
    }

    /**
     * 执行更新SQL（INSERT, UPDATE, DELETE等）
     *
     * @param sql 要执行的SQL语句
     * @return 受影响的行数
     */
    @Throws(SQLException::class)
    fun executeUpdate(
        sql: String,
        params: List<Any?> = emptyList(),
    ): Int {
        val conn = getConnection()
        prepareStatement(conn, sql, params).use { statement ->
            return statement.executeUpdate()
        }
    }

    /**
     * 执行任意SQL语句
     *
     * @param sql 要执行的SQL语句
     */
    @Throws(SQLException::class)
    fun execute(
        sql: String,
        params: List<Any?> = emptyList(),
    ) {
        val conn = getConnection()
        prepareStatement(conn, sql, params).use { statement ->
            statement.execute()
        }
    }

    /**
     * 关闭数据库连接
     */
    override fun close() {
        try {
            connection?.close()
        } catch (e: SQLException) {
            e.printStackTrace()
        }
    }

    private fun registerDriver() {
        driver?.trim()?.takeIf(String::isNotBlank)?.let { driverClassName ->
            Class.forName(driverClassName)
            return
        }
        when {
            url.startsWith("jdbc:mysql") -> Class.forName("com.mysql.cj.jdbc.Driver")
            url.startsWith("jdbc:postgresql") -> Class.forName("org.postgresql.Driver")
            url.startsWith("jdbc:oracle") -> Class.forName("oracle.jdbc.driver.OracleDriver")
            url.startsWith("jdbc:sqlserver") -> Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver")
            url.startsWith("jdbc:h2") -> Class.forName("org.h2.Driver")
            url.startsWith("jdbc:sqlite") -> Class.forName("org.sqlite.JDBC")
            url.startsWith("jdbc:dm") -> Class.forName("dm.jdbc.driver.DmDriver")
            url.startsWith("jdbc:kingbase8") -> Class.forName("com.kingbase8.Driver")
            url.startsWith("jdbc:gaussdb") -> Class.forName("com.huawei.gauss.jdbc.ZenithDriver")
            url.startsWith("jdbc:oceanbase") -> Class.forName("com.oceanbase.jdbc.Driver")
            url.startsWith("jdbc:polardb") -> Class.forName("com.aliyun.polardb.Driver")
            url.startsWith("jdbc:tidb") -> Class.forName("io.tidb.jdbc.TiDBDriver")
            url.startsWith("jdbc:db2") -> Class.forName("com.ibm.db2.jcc.DB2Driver")
            url.startsWith("jdbc:sybase") -> Class.forName("com.sybase.jdbc4.jdbc.SybDriver")
            url.startsWith("jdbc:TAOS-RS") -> Class.forName("com.taosdata.jdbc.rs.RestfulDriver")
            else -> throw IllegalArgumentException("Unsupported database type: $url")
        }
    }

    private fun prepareStatement(
        connection: Connection,
        sql: String,
        params: List<Any?>,
    ): PreparedStatement {
        return connection.prepareStatement(sql).apply {
            bindParams(params)
        }
    }

    private fun PreparedStatement.bindParams(
        params: List<Any?>,
    ) {
        params.forEachIndexed { index, value ->
            setObject(index + 1, value)
        }
    }
}

private fun ResultSet.toRowMap(): Map<String, Any?> {
    val columnCount = metaData.columnCount
    val row = linkedMapOf<String, Any?>()
    for (index in 1..columnCount) {
        val columnName = metaData.getColumnLabel(index)
        row[columnName] = getObject(index)
    }
    return row
}
