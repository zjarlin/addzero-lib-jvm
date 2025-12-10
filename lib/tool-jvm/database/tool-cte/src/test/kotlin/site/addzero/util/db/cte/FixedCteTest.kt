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

class FixedCteTest {

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

        // 使用反射设置私有属性，加入我们的新策略
        setPrivateField(cteUtil, "jdbcTemplate", jdbcTemplate)
        setPrivateField(cteUtil, "ctes", listOf(
            SimpleMySQLCteStrategy(), // 使用我们的新策略
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
    fun testFixedRecursiveTreeQuerySqlUp() {
        // 准备测试数据
        val tableName = "iot_device_info"
        val id = "device_id"
        val pid = "parent_device_id"
        val databaseType = DatabaseType.MYSQL
        
        // 使用一个简单的查询条件
        val cteWrapperContext = WrapperContext("WHERE device_id IN ('CNC2160', 'CNC2161', 'CNC634')", emptyMap())
        val combinedDataWrapperContext = WrapperContext("", emptyMap())
        val returnBreadcrumb = true
        val breadcrumbColumn = "device_id"

        try {
            val result = cteUtil.recursiveTreeQuerySqlUp(
                tableName, id, pid, databaseType,
                cteWrapperContext, combinedDataWrapperContext,
                returnBreadcrumb, breadcrumbColumn
            )

            println("查询结果数量: ${result.size}")
            result.forEach { row ->
                println("ID: ${row["device_id"]}, Parent ID: ${row["parent_device_id"]}, Breadcrumb: ${row["tree_breadcrumb"]}, Depth: ${row["tree_depth"]}")
            }
        } catch (e: Exception) {
            println("查询出错: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun setPrivateField(obj: Any, fieldName: String, value: Any?) {
        val field = obj.javaClass.getDeclaredField(fieldName)
        field.isAccessible = true
        field.set(obj, value)
    }
}