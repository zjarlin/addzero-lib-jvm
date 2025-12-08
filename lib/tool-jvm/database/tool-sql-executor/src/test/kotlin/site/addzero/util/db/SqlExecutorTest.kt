package site.addzero.util.db

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SqlExecutorTest {

    @Test
    fun testSqlExecutor() {
        val url = "jdbc:mysql://192.168.1.140:3306/iot_db?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=true&serverTimezone=GMT%2B8"
        val username = "root"
        val password = "zljkj~123"

        val sqlExecutor = SqlExecutor(url, username, password)

        try {
            val result = sqlExecutor.queryForList("SELECT 1 as test")
            println("查询结果: $result")

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
    
    @Test
    fun `test H2 in-memory database with arbitrary SQL`() {
        val url = "jdbc:h2:mem:store-db;database_to_upper=true"
        val username = "sa"
        val password = ""
        
        SqlExecutor(url, username, password).use { executor ->
            // 创建表
            executor.execute("""
                CREATE TABLE IF NOT EXISTS products (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    name VARCHAR(255) NOT NULL,
                    price DECIMAL(10, 2) NOT NULL,
                    stock INT DEFAULT 0,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
            """.trimIndent())
            
            // 插入测试数据
            listOf(
                "INSERT INTO products (name, price, stock) VALUES ('Laptop', 5999.99, 10)",
                "INSERT INTO products (name, price, stock) VALUES ('Mouse', 99.99, 50)",
                "INSERT INTO products (name, price, stock) VALUES ('Keyboard', 299.99, 30)"
            ).forEach(executor::execute)
            
            // 查询所有产品
            executor.queryForList("SELECT * FROM products ORDER BY id")
                .also { products ->
                    assertEquals(3, products.size, "应该有3个产品")
                    products.forEach { println("产品: $it") }
                }
            
            // 条件查询
            executor.queryForList("SELECT * FROM products WHERE price > 100")
                .also { expensiveProducts ->
                    assertEquals(2, expensiveProducts.size, "价格>100的产品应该有2个")
                    assertTrue(expensiveProducts.all { (it["PRICE"] as? Number)?.toDouble()?.let { price -> price > 100 } == true })
                }
            
            // 聚合查询
            executor.queryForList("SELECT COUNT(*) as total, SUM(stock) as total_stock FROM products")
                .first()
                .also { result ->
                    assertEquals(3, (result["TOTAL"] as Number).toInt(), "总产品数应为3")
                    assertEquals(90, (result["TOTAL_STOCK"] as Number).toInt(), "总库存应为90")
                }
            
            // 更新操作
            executor.executeUpdate("UPDATE products SET stock = stock + 5 WHERE name = 'Laptop'")
                .also { affected -> assertEquals(1, affected, "应该更新1条记录") }
            
            // 验证更新
            executor.queryForList("SELECT stock FROM products WHERE name = 'Laptop'")
                .first()["STOCK"]
                .also { stock -> assertEquals(15, (stock as Number).toInt(), "Laptop库存应为15") }
            
            // 复杂查询：JOIN和子查询（在删除之前先创建订单表和数据）
            executor.execute("""
                CREATE TABLE orders (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    product_id BIGINT,
                    quantity INT,
                    FOREIGN KEY (product_id) REFERENCES products(id)
                )
            """.trimIndent())
            
            executor.execute("INSERT INTO orders (product_id, quantity) VALUES (1, 2), (2, 5)")
            
            // 删除操作（删除没有订单的产品）
            executor.executeUpdate("DELETE FROM products WHERE id = 3")
                .also { affected -> assertEquals(1, affected, "应该删除1条记录") }
            
            // 验证删除
            executor.queryForList("SELECT COUNT(*) as count FROM products")
                .first()["COUNT"]
                .also { count -> assertEquals(2, (count as Number).toInt(), "剩余产品应为2个") }
            
            executor.queryForList("""
                SELECT p.name, p.price, o.quantity, (p.price * o.quantity) as total
                FROM products p
                JOIN orders o ON p.id = o.product_id
                ORDER BY total DESC
            """.trimIndent())
                .also { orderDetails ->
                    assertEquals(2, orderDetails.size)
                    println("订单详情:")
                    orderDetails.forEach { println("  $it") }
                }
            
            // 事务测试：批量操作
            listOf(
                "CREATE TABLE logs (id INT PRIMARY KEY, message VARCHAR(255))",
                "INSERT INTO logs VALUES (1, 'Test log 1')",
                "INSERT INTO logs VALUES (2, 'Test log 2')"
            ).forEach(executor::execute)
            
            executor.queryForList("SELECT * FROM logs")
                .also { logs ->
                    assertEquals(2, logs.size)
                    logs.forEach { println("日志: $it") }
                }
            
            println("✅ H2内存数据库任意SQL执行测试通过!")
        }
    }
    
    @Test
    fun `test H2 with DDL operations`() {
        SqlExecutor("jdbc:h2:mem:test-ddl", "sa", "").use { executor ->
            // 创建多个表
            executor.execute("""
                CREATE TABLE users (
                    id BIGINT PRIMARY KEY,
                    username VARCHAR(50) UNIQUE NOT NULL,
                    email VARCHAR(100)
                )
            """.trimIndent())
            
            executor.execute("""
                CREATE TABLE user_profiles (
                    user_id BIGINT PRIMARY KEY,
                    bio TEXT,
                    avatar_url VARCHAR(500),
                    FOREIGN KEY (user_id) REFERENCES users(id)
                )
            """.trimIndent())
            
            // ALTER TABLE 操作
            executor.execute("ALTER TABLE users ADD COLUMN created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
            
            // 插入数据
            executor.execute("INSERT INTO users (id, username, email) VALUES (1, 'alice', 'alice@example.com')")
            executor.execute("INSERT INTO user_profiles (user_id, bio) VALUES (1, 'Hello, I am Alice')")
            
            // 查询验证
            executor.queryForList("""
                SELECT u.username, u.email, p.bio
                FROM users u
                LEFT JOIN user_profiles p ON u.id = p.user_id
            """.trimIndent())
                .first()
                .also { user ->
                    assertEquals("alice", user["USERNAME"])
                    assertEquals("Hello, I am Alice", user["BIO"])
                }
            
            // DROP 操作
            executor.execute("DROP TABLE user_profiles")
            executor.execute("DROP TABLE users")
            
            println("✅ H2 DDL操作测试通过!")
        }
    }
    
    @Test
    fun `test H2 with stored procedures and functions`() {
        SqlExecutor("jdbc:h2:mem:test-functions", "sa", "").use { executor ->
            // 创建自定义函数
            executor.execute("""
                CREATE ALIAS CALCULATE_TAX AS $$
                Double calculateTax(Double amount) {
                    return amount * 0.13;
                }
                $$;
            """.trimIndent())
            
            // 使用自定义函数
            executor.queryForList("SELECT CALCULATE_TAX(100.0) as tax")
                .first()["TAX"]
                .also { tax ->
                    assertEquals(13.0, (tax as Number).toDouble(), 0.01)
                }
            
            println("✅ H2 自定义函数测试通过!")
        }
    }
}

/**
 * Extension function to use SqlExecutor with automatic resource management
 */
private inline fun <R> SqlExecutor.use(block: (SqlExecutor) -> R): R {
    return try {
        block(this)
    } finally {
        this.close()
    }
}