package site.addzero.util.db

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * H2数据库兼容性测试
 * 测试SqlExecutor与H2数据库的兼容性
 */
class SqlExecutorH2Test {

    @Test
    fun `test H2 in-memory database basic operations`() {
        val url = "jdbc:h2:mem:h2-test;DB_CLOSE_DELAY=-1"
        val username = "sa"
        val password = ""

        val executor = SqlExecutor(url, username, password)
        try {
            // 创建表 - 使用H2语法
            executor.execute("""
                CREATE TABLE test_table (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    name VARCHAR(255) NOT NULL,
                    age INT,
                    salary DECIMAL(10,2),
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
            """.trimIndent())

            // 插入数据
            executor.executeUpdate("INSERT INTO test_table (name, age, salary) VALUES ('Alice', 25, 5000.50)")
            executor.executeUpdate("INSERT INTO test_table (name, age, salary) VALUES ('Bob', 30, 6000.75)")
            executor.executeUpdate("INSERT INTO test_table (name, age, salary) VALUES ('Charlie', 35, 7000.25)")

            // 查询所有数据
            val results = executor.queryForList("SELECT * FROM test_table ORDER BY id")
            assertEquals(3, results.size, "应该有3条记录")

            // 验证数据
            val firstRecord = results[0]
            assertEquals("Alice", firstRecord["NAME"])
            assertEquals(25, (firstRecord["AGE"] as Number).toInt())
            assertEquals(5000.50, (firstRecord["SALARY"] as Number).toDouble(), 0.01)

            // 条件查询
            val youngEmployees = executor.queryForList("SELECT * FROM test_table WHERE age < 30")
            assertEquals(1, youngEmployees.size, "应该只有1个年龄小于30的员工")

            // 聚合查询
            val stats = executor.queryForList("SELECT COUNT(*) as total, AVG(salary) as avg_salary FROM test_table")
                .first()
            assertEquals(3, (stats["TOTAL"] as Number).toInt())
            assertEquals(6000.50, (stats["AVG_SALARY"] as Number).toDouble(), 0.01)

            println("✅ H2内存数据库基本操作测试通过!")
        } finally {
            executor.close()
        }
    }

    @Test
    fun `test H2 file database operations`() {
        val url = "jdbc:h2:./target/test-db/h2-file-test;DB_CLOSE_DELAY=-1"
        val username = "sa"
        val password = ""

        val executor = SqlExecutor(url, username, password)
        try {
            // 删除已存在的表（如果存在）
            executor.execute("DROP TABLE IF EXISTS employees")
            executor.execute("DROP TABLE IF EXISTS departments")

            // 创建复杂表结构 - 使用H2语法
            executor.execute("""
                CREATE TABLE departments (
                    id BIGINT PRIMARY KEY,
                    name VARCHAR(100) NOT NULL UNIQUE,
                    location VARCHAR(200)
                )
            """.trimIndent())

            executor.execute("""
                CREATE TABLE employees (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    name VARCHAR(100) NOT NULL,
                    department_id BIGINT,
                    hire_date DATE,
                    salary DECIMAL(10,2),
                    FOREIGN KEY (department_id) REFERENCES departments(id)
                )
            """.trimIndent())

            // 插入部门数据
            val departments = listOf(
                "INSERT INTO departments VALUES (1, 'Engineering', 'Building A')",
                "INSERT INTO departments VALUES (2, 'Marketing', 'Building B')",
                "INSERT INTO departments VALUES (3, 'HR', 'Building C')"
            )
            departments.forEach { executor.executeUpdate(it) }

            // 插入员工数据
            val employees = listOf(
                "INSERT INTO employees (name, department_id, hire_date, salary) VALUES ('John Doe', 1, '2023-01-15', 8000.00)",
                "INSERT INTO employees (name, department_id, hire_date, salary) VALUES ('Jane Smith', 2, '2023-02-20', 7500.00)",
                "INSERT INTO employees (name, department_id, hire_date, salary) VALUES ('Mike Johnson', 1, '2023-03-10', 8500.00)"
            )
            employees.forEach { executor.executeUpdate(it) }

            // JOIN查询
            val deptEmps = executor.queryForList("""
                SELECT d.name as dept_name, e.name as emp_name, e.salary
                FROM employees e
                JOIN departments d ON e.department_id = d.id
                ORDER BY d.name, e.salary DESC
            """.trimIndent())

            assertEquals(3, deptEmps.size)

            // 更新操作
            executor.executeUpdate("UPDATE employees SET salary = salary * 1.1 WHERE department_id = 1")
                .also { affected -> assertEquals(2, affected, "应该更新2个员工") }

            // 验证更新
            val updatedSalary = executor.queryForList("SELECT salary FROM employees WHERE name = 'John Doe'")
                .first()["SALARY"] as Number
            assertEquals(8800.00, updatedSalary.toDouble(), 0.01)

            println("✅ H2文件数据库操作测试通过!")
        } finally {
            executor.close()
        }
    }

    @Test
    fun `test H2 advanced SQL features`() {
        val url = "jdbc:h2:mem:h2-advanced;DB_CLOSE_DELAY=-1"
        val username = "sa"
        val password = ""

        val executor = SqlExecutor(url, username, password)
        try {
            // 创建测试数据
            executor.execute("""
                CREATE TABLE orders (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    customer_name VARCHAR(100),
                    amount DECIMAL(10,2),
                    order_date DATE,
                    status VARCHAR(20) DEFAULT 'pending'
                )
            """.trimIndent())

            val orders = listOf(
                "INSERT INTO orders (customer_name, amount, order_date, status) VALUES ('Alice', 100.00, '2023-01-01', 'completed')",
                "INSERT INTO orders (customer_name, amount, order_date, status) VALUES ('Bob', 200.00, '2023-01-02', 'shipped')",
                "INSERT INTO orders (customer_name, amount, order_date, status) VALUES ('Alice', 150.00, '2023-01-03', 'completed')",
                "INSERT INTO orders (customer_name, amount, order_date, status) VALUES ('Charlie', 300.00, '2023-01-04', 'pending')"
            )
            orders.forEach { executor.executeUpdate(it) }

            // 测试窗口函数
            val windowResults = executor.queryForList("""
                SELECT
                    customer_name,
                    amount,
                    order_date,
                    status,
                    SUM(amount) OVER (PARTITION BY customer_name) as customer_total,
                    RANK() OVER (ORDER BY amount DESC) as amount_rank
                FROM orders
                ORDER BY customer_total DESC, amount DESC
            """.trimIndent())

            assertEquals(4, windowResults.size)

            // 测试GROUP BY和HAVING
            val groupResults = executor.queryForList("""
                SELECT
                    customer_name,
                    COUNT(*) as order_count,
                    SUM(amount) as total_amount,
                    AVG(amount) as avg_amount
                FROM orders
                GROUP BY customer_name
                HAVING COUNT(*) > 1
                ORDER BY total_amount DESC
            """.trimIndent())

            assertEquals(1, groupResults.size, "应该只有Alice有多个订单")
            assertEquals("Alice", groupResults.first()["CUSTOMER_NAME"])

            // 测试CASE语句
            val caseResults = executor.queryForList("""
                SELECT
                    customer_name,
                    amount,
                    CASE
                        WHEN status = 'completed' THEN 'Done'
                        WHEN status = 'shipped' THEN 'In Transit'
                        WHEN status = 'pending' THEN 'Waiting'
                        ELSE 'Unknown'
                    END as status_text,
                    CASE
                        WHEN amount > 250 THEN 'High Value'
                        WHEN amount > 100 THEN 'Medium Value'
                        ELSE 'Low Value'
                    END as value_category
                FROM orders
                ORDER BY amount DESC
            """.trimIndent())

            assertEquals(4, caseResults.size)

            println("✅ H2高级SQL特性测试通过!")
        } finally {
            executor.close()
        }
    }

    @Test
    fun `test H2 error handling`() {
        val url = "jdbc:h2:mem:h2-error-test;DB_CLOSE_DELAY=-1"
        val username = "sa"
        val password = ""

        val executor = SqlExecutor(url, username, password)
        try {
            // 测试语法错误
            assertThrows<Exception> {
                executor.execute("INVALID SQL STATEMENT")
            }

            // 测试表不存在错误
            assertThrows<Exception> {
                executor.queryForList("SELECT * FROM non_existent_table")
            }

            // 测试约束违反 - 修复主键约束测试
            executor.execute("CREATE TABLE test_constraint (id BIGINT PRIMARY KEY, name VARCHAR(100) UNIQUE)")
            executor.executeUpdate("INSERT INTO test_constraint (id, name) VALUES (1, 'test')")

            assertThrows<Exception> {
                executor.executeUpdate("INSERT INTO test_constraint (id, name) VALUES (1, 'duplicate')")
            }

            println("✅ H2错误处理测试通过!")
        } finally {
            executor.close()
        }
    }

    @Test
    fun `test H2 performance with large dataset`() {
        val url = "jdbc:h2:mem:h2-performance;DB_CLOSE_DELAY=-1"
        val username = "sa"
        val password = ""

        val executor = SqlExecutor(url, username, password)
        try {
            // 创建测试表
            executor.execute("""
                CREATE TABLE performance_test (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    data VARCHAR(1000),
                    val_value INT,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
            """.trimIndent())

            // 批量插入1000条数据
            val batchSize = 1000
            val startTime = System.currentTimeMillis()

            repeat(batchSize) { i ->
                executor.executeUpdate("""
                    INSERT INTO performance_test (data, val_value)
                    VALUES ('Test data ${i}', ${i * 10})
                """.trimIndent())
            }

            val insertTime = System.currentTimeMillis() - startTime
            println("H2插入${batchSize}条数据耗时: ${insertTime}ms")

            // 测试查询性能
            val queryStart = System.currentTimeMillis()
            val results = executor.queryForList("SELECT * FROM performance_test WHERE val_value > 5000")
            val queryTime = System.currentTimeMillis() - queryStart

            assertEquals(499, results.size, "应该有499条记录val_value > 5000")
            println("H2查询${results.size}条数据耗时: ${queryTime}ms")

            // 测试索引查询性能
            executor.execute("CREATE INDEX idx_performance_value ON performance_test(val_value)")

            val indexedQueryStart = System.currentTimeMillis()
            val indexedResults = executor.queryForList("SELECT * FROM performance_test WHERE val_value BETWEEN 2000 AND 3000")
            val indexedQueryTime = System.currentTimeMillis() - indexedQueryStart

            assertEquals(101, indexedResults.size, "应该有101条记录在2000-3000之间")
            println("H2索引查询耗时: ${indexedQueryTime}ms")

            println("✅ H2性能测试通过!")
        } finally {
            executor.close()
        }
    }
}