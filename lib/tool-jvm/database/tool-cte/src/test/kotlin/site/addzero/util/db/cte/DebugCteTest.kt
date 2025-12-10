package site.addzero.util.db.cte

import com.mysql.cj.jdbc.MysqlDataSource
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.jdbc.core.JdbcTemplate
import site.addzero.util.db.DatabaseType
import site.addzero.util.db.SqlExecutor
import site.addzero.util.db.cte.strategy.impl.*
import site.addzero.util.db.wrapper.entity.WrapperContext
import java.sql.Connection
import java.sql.DriverManager

class DebugCteTest {

    private lateinit var cteUtil: CteUtil
    private lateinit var jdbcTemplate: JdbcTemplate
    private lateinit var sqlExecutor: SqlExecutor

    @BeforeEach
    fun setUp() {
        // 创建数据源
        val url = "jdbc:mysql://192.168.1.140:3306/iot_db?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=true&serverTimezone=GMT%2B8"
        val username = "root"
        val password = "zljkj~123"

        // 使用我们新创建的SqlExecutor执行一些初始化SQL
        sqlExecutor = SqlExecutor(url, username, password)

        // 创建Spring JDBC数据源用于CteUtil
        val dataSource = MysqlDataSource()
        dataSource.setUrl(url)
        dataSource.setUser(username)
        dataSource.setPassword(password)

        jdbcTemplate = JdbcTemplate(dataSource)

        cteUtil = CteUtil()

        // 使用反射设置私有属性
        setPrivateField(cteUtil, "jdbcTemplate", jdbcTemplate)
        setPrivateField(cteUtil, "ctes", listOf(
            MySQLCteStrategy(),
            PostgreSQLCteStrategy(),
            OracleCteStrategy(),
            SqlServerCteStrategy(),
            DamengCteStrategy(),
            KingbaseCteStrategy(),
            GaussDbCteStrategy()
        ))
    }

    @AfterEach
    fun tearDown() {
        sqlExecutor.close()
    }

    @Test
    fun debugCteQuery() {
        // 直接连接数据库执行原始SQL查询，验证数据结构
        val url = "jdbc:mysql://192.168.1.140:3306/iot_db?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=true&serverTimezone=GMT%2B8"
        val username = "root"
        val password = "zljkj~123"
        
        val connection = DriverManager.getConnection(url, username, password)
        
        try {
            // 查询特定设备及其父设备信息
            val stmt = connection.createStatement()
            val rs = stmt.executeQuery("""
                SELECT 
                    d1.device_id,
                    d1.parent_device_id,
                    d2.device_id as parent_device_exists
                FROM iot_device_info d1
                LEFT JOIN iot_device_info d2 ON d1.parent_device_id = d2.device_id
                WHERE d1.device_id IN ('CNC2160', 'CNC2161', 'CNC634')
            """.trimIndent())
            
            println("=== 原始数据验证 ===")
            while (rs.next()) {
                val deviceId = rs.getString("device_id")
                val parentId = rs.getString("parent_device_id")
                val parentExists = rs.getString("parent_device_exists")
                
                println("设备ID: $deviceId, 父设备ID: $parentId, 父设备存在: $parentExists")
            }
            
            rs.close()
            
            // 执行CTE查询并分析结果
            println("\n=== CTE查询结果 ===")
            val tableName = "iot_device_info"
            val id = "device_id"
            val pid = "parent_device_id"
            val databaseType = DatabaseType.MYSQL
            
            // 使用相同的条件测试CTE
            val cteWrapperContext = WrapperContext("WHERE device_id IN ('CNC2160', 'CNC2161', 'CNC634')", emptyMap())
            val combinedDataWrapperContext = WrapperContext("", emptyMap())
            val returnBreadcrumb = true
            val breadcrumbColumn = "device_id"

            val result = cteUtil.recursiveTreeQuerySqlUp(
                tableName, id, pid, databaseType,
                cteWrapperContext, combinedDataWrapperContext,
                returnBreadcrumb, breadcrumbColumn
            )
            
            result.forEach { row ->
                println("ID: ${row["device_id"]}, Parent ID: ${row["parent_device_id"]}, Breadcrumb: ${row["tree_breadcrumb"]}, Depth: ${row["tree_depth"]}")
            }
            
        } finally {
            connection.close()
        }
    }

    private fun setPrivateField(obj: Any, fieldName: String, value: Any?) {
        val field = obj.javaClass.getDeclaredField(fieldName)
        field.isAccessible = true
        field.set(obj, value)
    }
}