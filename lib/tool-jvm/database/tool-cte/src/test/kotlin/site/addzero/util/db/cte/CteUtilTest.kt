package site.addzero.util.db.cte

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.jdbc.core.JdbcTemplate
import site.addzero.util.db.DatabaseType
import site.addzero.util.db.cte.strategy.impl.MySQLCteStrategy
import site.addzero.util.db.wrapper.entity.WrapperContext

import org.h2.jdbcx.JdbcDataSource

class CteUtilTest {

    private lateinit var cteUtil: CteUtil
    private lateinit var jdbcTemplate: JdbcTemplate

    @BeforeEach
    fun setUp() {
        // 手动构造JdbcTemplate实例，而不是使用@Autowired和@Mock
        val dataSource = JdbcDataSource()
        dataSource.setURL("jdbc:h2:mem:testdb")
        dataSource.setUser("sa")
        dataSource.setPassword("")
        jdbcTemplate = JdbcTemplate(dataSource)

        // 使用反射设置私有字段
        cteUtil = CteUtil()
        setPrivateField(cteUtil, "jdbcTemplate", jdbcTemplate)



        // 创建测试表和数据
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS device (id INT PRIMARY KEY, parent_id INT, name VARCHAR(255), status VARCHAR(50))")
        jdbcTemplate.execute("INSERT INTO device (id, parent_id, name, status) VALUES (1, null, 'Root', 'active')")
        jdbcTemplate.execute("INSERT INTO device (id, parent_id, name, status) VALUES (2, 1, 'Child1', 'active')")
        jdbcTemplate.execute("INSERT INTO device (id, parent_id, name, status) VALUES (3, 2, 'Child2', 'active')")

    }

    private fun setPrivateField(target: Any, fieldName: String, value: Any) {
        val field = target.javaClass.getDeclaredField(fieldName)
        field.isAccessible = true
        field.set(target, value)
    }

    @Test
    fun `test recursiveTreeQuerySqlUp with MySQL database`() {
        // 准备测试数据
        val tableName = "device"
        val id = "id"
        val pid = "parent_id"
        val databaseType = DatabaseType.MYSQL
        val cteWrapperContext = WrapperContext("", emptyMap())
        val combinedDataWrapperContext = WrapperContext("", emptyMap())
        val returnBreadcrumb = true
        val breadcrumbColumn = "name"


        // 执行测试
        val result = cteUtil.recursiveTreeQuerySqlUp(
            tableName, id, pid, databaseType, cteWrapperContext, combinedDataWrapperContext, returnBreadcrumb, breadcrumbColumn
        )

        // 验证结果不为空
        assert(result.isNotEmpty())

        // 清理测试数据
        jdbcTemplate.execute("DROP TABLE device")
    }

    @Test
    fun `test recursiveTreeQuerySqlUpAndDown with MySQL database`() {
        // 准备测试数据
        val tableName = "device"
        val id = "id"
        val pid = "parent_id"
        val databaseType = DatabaseType.MYSQL
        val cteWrapperContext = WrapperContext("", emptyMap())
        val combinedDataWrapperContext = WrapperContext("", emptyMap())
        val returnBreadcrumb = true
        val breadcrumbColumn = "name"

        // 创建测试表和数据
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS device (id INT PRIMARY KEY, parent_id INT, name VARCHAR(255), status VARCHAR(50))")
        jdbcTemplate.execute("INSERT INTO device (id, parent_id, name, status) VALUES (1, null, 'Root', 'active')")
        jdbcTemplate.execute("INSERT INTO device (id, parent_id, name, status) VALUES (2, 1, 'Child1', 'active')")
        jdbcTemplate.execute("INSERT INTO device (id, parent_id, name, status) VALUES (3, 2, 'Child2', 'active')")

        // 执行测试
        val result = cteUtil.recursiveTreeQuerySqlUpAndDown(
            tableName, id, pid, databaseType, cteWrapperContext, combinedDataWrapperContext, returnBreadcrumb, breadcrumbColumn
        )

        // 验证结果不为空
        assert(result.isNotEmpty())

        // 清理测试数据
        jdbcTemplate.execute("DROP TABLE device")
    }

    @Test
    fun `test actStrategy with supported database`() {
        // 执行测试
        val result = cteUtil.actStrategy(DatabaseType.MYSQL)

        // 验证结果
        assert(result is MySQLCteStrategy)
    }

    @Test
    fun `test actStrategy with unsupported database`() {
        // 执行测试并验证异常
        try {
            cteUtil.actStrategy(DatabaseType.DB2)
            assert(false) { "Expected IllegalArgumentException was not thrown" }
        } catch (e: IllegalArgumentException) {
            assert(e.message == "No CTE strategy found")
        }
    }
}
