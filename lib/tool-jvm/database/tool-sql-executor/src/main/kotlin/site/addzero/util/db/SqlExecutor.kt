package site.addzero.util.db

import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException

/**
 * 轻量级SQL执行器工具类
 * 支持多种数据库方言，不依赖任何框架
 */
class SqlExecutor(
    private val url: String,
    private val username: String,
    private val password: String
) {
    private var connection: Connection? = null

    init {
        // 注册JDBC驱动
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
            else -> throw IllegalArgumentException("Unsupported database type: $url")
        }
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
     * 执行查询SQL并返回结果列表
     *
     * @param sql 要执行的SQL语句
     * @return 查询结果列表，每个元素是一个包含列名和值的Map
     */
    @Throws(SQLException::class)
    fun queryForList(sql: String): List<Map<String, Any?>> {
        val conn = getConnection()
        val statement = conn.createStatement()
        val resultSet = statement.executeQuery(sql)
        val columnCount = resultSet.metaData.columnCount

        val result = mutableListOf<Map<String, Any?>>()
        while (resultSet.next()) {
            val row = mutableMapOf<String, Any?>()
            for (i in 1..columnCount) {
                val columnName = resultSet.metaData.getColumnName(i)
                val value = resultSet.getObject(i)
                row[columnName] = value
            }
            result.add(row)
        }

        resultSet.close()
        statement.close()

        return result
    }

    /**
     * 执行更新SQL（INSERT, UPDATE, DELETE等）
     *
     * @param sql 要执行的SQL语句
     * @return 受影响的行数
     */
    @Throws(SQLException::class)
    fun executeUpdate(sql: String): Int {
        val conn = getConnection()
        val statement = conn.createStatement()
        val result = statement.executeUpdate(sql)
        statement.close()
        return result
    }

    /**
     * 执行任意SQL语句
     *
     * @param sql 要执行的SQL语句
     */
    @Throws(SQLException::class)
    fun execute(sql: String) {
        val conn = getConnection()
        val statement = conn.createStatement()
        statement.execute(sql)
        statement.close()
    }

    /**
     * 关闭数据库连接
     */
    fun close() {
        try {
            connection?.close()
        } catch (e: SQLException) {
            e.printStackTrace()
        }
    }
}
