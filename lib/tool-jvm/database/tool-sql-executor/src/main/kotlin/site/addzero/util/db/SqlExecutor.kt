package site.addzero.util.db

import java.sql.Connection
import java.sql.DriverManager
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException
import javax.sql.DataSource

/**
 * 轻量级SQL执行器工具类
 * 支持多种数据库方言，不依赖任何框架
 */
class SqlExecutor private constructor(
    private val url: String?,
    private val username: String,
    private val password: String,
    private val driver: String?,
    private val dataSource: DataSource?,
) : AutoCloseable {
    private var connection: Connection? = null
    private val transactionalConnection = ThreadLocal<Connection?>()

    constructor(
        url: String,
        username: String = "",
        password: String = "",
        driver: String? = null,
    ) : this(
        url = url,
        username = username,
        password = password,
        driver = driver,
        dataSource = null,
    ) {
        registerDriver()
    }

    constructor(
        dataSource: DataSource,
    ) : this(
        url = null,
        username = "",
        password = "",
        driver = null,
        dataSource = dataSource,
    )

    /**
     * 获取数据库连接
     */
    @Throws(SQLException::class)
    private fun getConnection(): Connection {
        if (connection == null || connection!!.isClosed) {
            connection = DriverManager.getConnection(requireNotNull(url), username, password)
        }
        return connection!!
    }

    /**
     * 在 JDBC 连接作用域里执行逻辑。
     *
     * DataSource 模式下每次调用独立申请并释放连接；
     * URL 模式下复用当前执行器持有的单连接生命周期。
     */
    private fun <T> withJdbcConnection(
        block: (Connection) -> T,
    ): T {
        val currentTransactionalConnection = transactionalConnection.get()
        if (currentTransactionalConnection != null && !currentTransactionalConnection.isClosed) {
            return block(currentTransactionalConnection)
        }
        val currentDataSource = dataSource
        if (currentDataSource != null) {
            return currentDataSource.connection.use(block)
        }
        return block(getConnection())
    }

    /**
     * 对外暴露连接作用域，统一复用连接生命周期。
     */
    fun <T> withConnection(
        block: (Connection) -> T,
    ): T {
        return withJdbcConnection(block)
    }

    /**
     * 在单连接事务中执行逻辑。
     */
    fun <T> withTransaction(
        block: (Connection) -> T,
    ): T {
        val currentTransactionalConnection = transactionalConnection.get()
        if (currentTransactionalConnection != null && !currentTransactionalConnection.isClosed) {
            return block(currentTransactionalConnection)
        }
        return withJdbcConnection { connection ->
            val originalAutoCommit = connection.autoCommit
            connection.autoCommit = false
            transactionalConnection.set(connection)
            try {
                val result = block(connection)
                connection.commit()
                result
            } catch (error: Throwable) {
                connection.rollback()
                throw error
            } finally {
                transactionalConnection.remove()
                connection.autoCommit = originalAutoCommit
            }
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
     * 执行查询 SQL 并返回结果列表。
     */
    @Throws(SQLException::class)
    fun queryForList(
        sql: String,
        vararg params: Any?,
    ): List<Map<String, Any?>> {
        return queryForList(sql, params.toList())
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
        return withJdbcConnection { connection ->
            prepareStatement(connection, sql, params).use { statement ->
                statement.executeQuery().use { resultSet ->
                    val result = mutableListOf<T>()
                    while (resultSet.next()) {
                        result += mapper(resultSet)
                    }
                    result
                }
            }
        }
    }

    /**
     * 执行参数化查询。
     */
    @Throws(SQLException::class)
    fun <T> query(
        sql: String,
        vararg params: Any?,
        mapper: (ResultSet) -> T,
    ): List<T> {
        return query(sql, params.toList(), mapper)
    }

    /**
     * 查询首列 Long 列表。
     */
    @Throws(SQLException::class)
    fun queryIds(
        sql: String,
        params: List<Any?> = emptyList(),
    ): MutableList<Long> {
        return query(sql, params) { resultSet ->
            resultSet.getLong(1)
        }.toMutableList()
    }

    /**
     * 查询首列 Long 列表。
     */
    @Throws(SQLException::class)
    fun queryIds(
        sql: String,
        vararg params: Any?,
    ): MutableList<Long> {
        return queryIds(sql, params.toList())
    }

    /**
     * 查询首列数量值。
     */
    @Throws(SQLException::class)
    fun queryCount(
        sql: String,
        params: List<Any?> = emptyList(),
    ): Long {
        return query(sql, params) { resultSet ->
            resultSet.getLong(1)
        }.firstOrNull() ?: 0L
    }

    /**
     * 查询首列数量值。
     */
    @Throws(SQLException::class)
    fun queryCount(
        sql: String,
        vararg params: Any?,
    ): Long {
        return queryCount(sql, params.toList())
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
        return withJdbcConnection { connection ->
            prepareStatement(connection, sql, params).use { statement ->
                statement.executeUpdate()
            }
        }
    }

    /**
     * 执行更新 SQL（INSERT、UPDATE、DELETE 等）。
     */
    @Throws(SQLException::class)
    fun executeUpdate(
        sql: String,
        vararg params: Any?,
    ): Int {
        return executeUpdate(sql, params.toList())
    }

    /**
     * 批量执行参数化更新。
     */
    @Throws(SQLException::class)
    fun batchUpdate(
        sql: String,
        batchParams: List<List<Any?>>,
    ): IntArray {
        if (batchParams.isEmpty()) {
            return intArrayOf()
        }
        return withJdbcConnection { connection ->
            connection.prepareStatement(sql).use { statement ->
                batchParams.forEach { params ->
                    statement.clearParameters()
                    statement.bindParams(params)
                    statement.addBatch()
                }
                statement.executeBatch()
            }
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
        withJdbcConnection { connection ->
            prepareStatement(connection, sql, params).use { statement ->
                statement.execute()
            }
        }
    }

    /**
     * 执行任意 SQL 语句。
     */
    @Throws(SQLException::class)
    fun execute(
        sql: String,
        vararg params: Any?,
    ) {
        execute(sql, params.toList())
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
        val jdbcUrl = requireNotNull(url)
        when {
            jdbcUrl.startsWith("jdbc:mysql") -> Class.forName("com.mysql.cj.jdbc.Driver")
            jdbcUrl.startsWith("jdbc:postgresql") -> Class.forName("org.postgresql.Driver")
            jdbcUrl.startsWith("jdbc:oracle") -> Class.forName("oracle.jdbc.driver.OracleDriver")
            jdbcUrl.startsWith("jdbc:sqlserver") -> Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver")
            jdbcUrl.startsWith("jdbc:h2") -> Class.forName("org.h2.Driver")
            jdbcUrl.startsWith("jdbc:sqlite") -> Class.forName("org.sqlite.JDBC")
            jdbcUrl.startsWith("jdbc:dm") -> Class.forName("dm.jdbc.driver.DmDriver")
            jdbcUrl.startsWith("jdbc:kingbase8") -> Class.forName("com.kingbase8.Driver")
            jdbcUrl.startsWith("jdbc:gaussdb") -> Class.forName("com.huawei.gauss.jdbc.ZenithDriver")
            jdbcUrl.startsWith("jdbc:oceanbase") -> Class.forName("com.oceanbase.jdbc.Driver")
            jdbcUrl.startsWith("jdbc:polardb") -> Class.forName("com.aliyun.polardb.Driver")
            jdbcUrl.startsWith("jdbc:tidb") -> Class.forName("io.tidb.jdbc.TiDBDriver")
            jdbcUrl.startsWith("jdbc:db2") -> Class.forName("com.ibm.db2.jcc.DB2Driver")
            jdbcUrl.startsWith("jdbc:sybase") -> Class.forName("com.sybase.jdbc4.jdbc.SybDriver")
            jdbcUrl.startsWith("jdbc:TAOS-RS") -> Class.forName("com.taosdata.jdbc.rs.RestfulDriver")
            else -> throw IllegalArgumentException("Unsupported database type: $jdbcUrl")
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
