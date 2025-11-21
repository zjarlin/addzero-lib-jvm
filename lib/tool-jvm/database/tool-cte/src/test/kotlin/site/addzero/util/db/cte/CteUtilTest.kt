package site.addzero.util.db.cte

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.jdbc.core.JdbcTemplate
import site.addzero.util.db.SqlExecutor
import site.addzero.util.db.DatabaseType
import site.addzero.util.db.cte.strategy.impl.*
import site.addzero.util.db.wrapper.entity.WrapperContext
import javax.sql.DataSource
import com.mysql.cj.jdbc.MysqlDataSource

class CteUtilTest {

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
    fun testRecursiveTreeQuerySqlUp() {
        // 准备测试数据
        val tableName = "iot_device_info"
        val id = "device_id"
        val pid = "parent_device_id"
        val databaseType = DatabaseType.MYSQL




        val cteWrapperContext = WrapperContext("WHERE 1 = 1", emptyMap())
        val combinedDataWrapperContext = WrapperContext("", emptyMap())
        val returnBreadcrumb = true
        val breadcrumbColumn = "device_id"

        try {
            // 执行测试
            val result = cteUtil.recursiveTreeQuerySqlUp(
                tableName, id, pid, databaseType,
                cteWrapperContext, combinedDataWrapperContext,
                returnBreadcrumb, breadcrumbColumn
            )

            // 验证结果
            println("查询结果数量: ${result.size}")
            result.forEach { row ->
                println("ID: ${row["id"]}, Parent ID: ${row["parent_id"]}, Name: ${row["name"]}, Breadcrumb: ${row["tree_breadcrumb"]}")
            }
        } catch (e: Exception) {
            println("测试执行过程中出现异常: ${e.message}")
            e.printStackTrace()
        }
    }

    @Test
    fun testRecursiveTreeQuerySqlUpAndDown() {
        // 准备测试数据
        val tableName = "departments"
        val id = "id"
        val pid = "parent_id"
        val databaseType = DatabaseType.MYSQL
        val cteWrapperContext = WrapperContext("WHERE id = 1", emptyMap())
        val combinedDataWrapperContext = WrapperContext("", emptyMap())
        val returnBreadcrumb = true
        val breadcrumbColumn = "name"

        try {
            // 执行测试
            val result = cteUtil.recursiveTreeQuerySqlUpAndDown(
                tableName, id, pid, databaseType,
                cteWrapperContext, combinedDataWrapperContext,
                returnBreadcrumb, breadcrumbColumn
            )

            // 验证结果
            println("查询结果数量: ${result.size}")
            result.forEach { row ->
                println("ID: ${row["id"]}, Parent ID: ${row["parent_id"]}, Name: ${row["name"]}, Breadcrumb: ${row["tree_breadcrumb"]}, Direction: ${row["tree_direction"]}, Depth: ${row["tree_depth"]}")
            }
        } catch (e: Exception) {
            println("测试执行过程中出现异常: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun setPrivateField(obj: Any, fieldName: String, value: Any?) {
        val field = obj.javaClass.getDeclaredField(fieldName)
        field.isAccessible = true
        field.set(obj, value)
    }
}
