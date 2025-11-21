package site.addzero.util.db

import org.junit.jupiter.api.Test

class SqlExecutorTest {

    @Test
    fun testSqlExecutor() {
        // 使用您提供的数据库连接参数
        val url = "jdbc:mysql://192.168.1.140:3306/iot_db?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=true&serverTimezone=GMT%2B8"
        val username = "root"
        val password = "zljkj~123"

        // 创建SQL执行器实例
        val sqlExecutor = SqlExecutor(url, username, password)

        try {
            // 示例：执行查询
            val result = sqlExecutor.queryForList("SELECT 1 as test")
            println("查询结果: $result")

            // 验证结果
            assert(result.isNotEmpty())
            assert(result[0]["test"] == 1)
            
            println("SQL执行器测试通过!")
        } catch (e: Exception) {
            println("测试执行过程中出现异常: ${e.message}")
            e.printStackTrace()
        } finally {
            sqlExecutor.close()
        }
    }
}