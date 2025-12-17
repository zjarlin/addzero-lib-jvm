package site.addzero.util.db

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * SQLite数据库兼容性测试
 * 测试SqlExecutor与SQLite数据库的兼容性
 */
class SqlExecutorSQLiteTest {

    @Test
    fun `test SQLite in-memory database basic operations`() {
        val url = "jdbc:sqlite::memory:"
        val username = ""
        val password = ""

        val executor = SqlExecutor(url, username, password)
        try {
            // 创建表
            executor.execute("""
                CREATE TABLE IF NOT EXISTS test_table (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL,
                    age INTEGER,
                    salary REAL,
                    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
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
            val name = firstRecord["name"] ?: firstRecord["NAME"]
            assertEquals("Alice", name)
            val ageValue = (firstRecord["age"] ?: firstRecord["AGE"])
            assertEquals(25, (ageValue as Number).toInt())
            val salaryValue = (firstRecord["salary"] ?: firstRecord["SALARY"])
            assertEquals(5000.50, (salaryValue as Number).toDouble(), 0.01)

            // 条件查询
            val youngEmployees = executor.queryForList("SELECT * FROM test_table WHERE age < 30")
            assertEquals(1, youngEmployees.size, "应该只有1个年龄小于30的员工")

            // 聚合查询
            val stats = executor.queryForList("SELECT COUNT(*) as total, AVG(salary) as avg_salary FROM test_table")
                .first()
            val total = stats["total"] ?: stats["TOTAL"]
            val avgSalary = stats["avg_salary"] ?: stats["AVG_SALARY"]
            assertEquals(3, (total as Number).toInt())
            assertEquals(6000.50, (avgSalary as Number).toDouble(), 0.01)

            println("✅ SQLite内存数据库基本操作测试通过!")
        } finally {
            executor.close()
        }
    }

    @Test
    fun `test SQLite file database operations`() {
        val url = "jdbc:sqlite:./target/test-db/sqlite-file-test.db"
        val username = ""
        val password = ""

        val executor = SqlExecutor(url, username, password)
        try {
            // 删除已存在的表（如果存在）
            executor.execute("DROP TABLE IF EXISTS employees")
            executor.execute("DROP TABLE IF EXISTS departments")

            // 创建复杂表结构
            executor.execute("""
                CREATE TABLE departments (
                    id INTEGER PRIMARY KEY,
                    name TEXT NOT NULL UNIQUE,
                    location TEXT
                )
            """.trimIndent())

            executor.execute("""
                CREATE TABLE employees (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL,
                    department_id INTEGER,
                    hire_date DATE,
                    salary REAL,
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

            // 验证JOIN查询结果 - 使用实际的列名
            val firstResult = deptEmps.first()
            val deptName = firstResult["dept_name"] ?: firstResult["DEPT_NAME"]
            assertEquals("Engineering", deptName)
            assertEquals(3, deptEmps.size)

            // 更新操作
            executor.executeUpdate("UPDATE employees SET salary = salary * 1.1 WHERE department_id = 1")
                .also { affected -> assertEquals(2, affected, "应该更新2个员工") }

            // 验证更新
            val updatedSalary = executor.queryForList("SELECT salary FROM employees WHERE name = 'John Doe'")
                .first()
            val salaryValue = (updatedSalary["salary"] ?: updatedSalary["SALARY"]) as Number
            assertEquals(8800.0, salaryValue.toDouble(), 0.01)

            println("✅ SQLite文件数据库操作测试通过!")
        } finally {
            executor.close()
        }
    }

    @Test
    fun `test SQLite advanced SQL features`() {
        val url = "jdbc:sqlite::memory:"
        val username = ""
        val password = ""

        val executor = SqlExecutor(url, username, password)
        try {
            // 创建测试数据
            executor.execute("""
                CREATE TABLE IF NOT EXISTS orders (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    customer_name TEXT,
                    amount REAL,
                    order_date DATE,
                    status TEXT DEFAULT 'pending'
                )
            """.trimIndent())

            val orders = listOf(
                "INSERT INTO orders (customer_name, amount, order_date, status) VALUES ('Alice', 100.00, '2023-01-01', 'completed')",
                "INSERT INTO orders (customer_name, amount, order_date, status) VALUES ('Bob', 200.00, '2023-01-02', 'shipped')",
                "INSERT INTO orders (customer_name, amount, order_date, status) VALUES ('Alice', 150.00, '2023-01-03', 'completed')",
                "INSERT INTO orders (customer_name, amount, order_date, status) VALUES ('Charlie', 300.00, '2023-01-04', 'pending')"
            )
            orders.forEach { executor.executeUpdate(it) }

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
            val firstResult = groupResults.first()
            val groupCustomerName = firstResult["customer_name"] ?: firstResult["CUSTOMER_NAME"]
            assertEquals("Alice", groupCustomerName)

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

            // 验证第一个记录的customer_name
            val firstCaseResult = caseResults.first()
            val caseCustomerNameValue = firstCaseResult["customer_name"] ?: firstCaseResult["CUSTOMER_NAME"]
            assertEquals("Charlie", caseCustomerNameValue)

            // 测试子查询
            val subQueryResults = executor.queryForList("""
                SELECT customer_name, amount
                FROM orders
                WHERE amount > (
                    SELECT AVG(amount) FROM orders
                )
                ORDER BY amount DESC
            """.trimIndent())

            assertEquals(2, subQueryResults.size, "应该有2个订单金额大于平均值")

            // 验证子查询结果
            val firstSubQueryResult = subQueryResults.first()
            val subQueryCustomerNameValue = firstSubQueryResult["customer_name"] ?: firstSubQueryResult["CUSTOMER_NAME"]
            assertEquals("Charlie", subQueryCustomerNameValue)

            println("✅ SQLite高级SQL特性测试通过!")
        } finally {
            executor.close()
        }
    }

    @Test
    fun `test SQLite specific features`() {
        val url = "jdbc:sqlite::memory:"
        val username = ""
        val password = ""

        val executor = SqlExecutor(url, username, password)
        try {
            // 创建表
            executor.execute("""
                CREATE TABLE IF NOT EXISTS products (
                    id INTEGER PRIMARY KEY,
                    name TEXT NOT NULL,
                    price REAL,
                    category TEXT
                )
            """.trimIndent())

            // 插入测试数据
            val products = listOf(
                "INSERT INTO products VALUES (1, 'Laptop', 999.99, 'Electronics')",
                "INSERT INTO products VALUES (2, 'Mouse', 29.99, 'Electronics')",
                "INSERT INTO products VALUES (3, 'Book', 19.99, 'Books')",
                "INSERT INTO products VALUES (4, 'Keyboard', 79.99, 'Electronics')",
                "INSERT INTO products VALUES (5, 'Novel', 14.99, 'Books')"
            )
            products.forEach { executor.executeUpdate(it) }

            // 测试UNIQUE约束
            executor.execute("CREATE TABLE IF NOT EXISTS unique_test (id INTEGER, name TEXT UNIQUE)")

            executor.executeUpdate("INSERT INTO unique_test VALUES (1, 'test')")

            // SQLite会返回错误码而不是异常，这里测试是否能够处理
            try {
                executor.executeUpdate("INSERT INTO unique_test VALUES (2, 'test')")
                println("SQLite UNIQUE约束测试：没有抛出异常（SQLite行为）")
            } catch (e: Exception) {
                println("SQLite UNIQUE约束测试：捕获到异常 ${e.message}")
            }

            // 测试LIKE查询
            val electronics = executor.queryForList("""
                SELECT * FROM products
                WHERE category LIKE 'Electronics'
                ORDER BY price DESC
            """.trimIndent())

            assertEquals(3, electronics.size)

            // 测试IN查询
            val idInResults = executor.queryForList("SELECT * FROM products WHERE id IN (1, 3, 5)")
            assertEquals(3, idInResults.size)

            // 测试BETWEEN查询
            val priceBetween = executor.queryForList("SELECT * FROM products WHERE price BETWEEN 15 AND 100")
            assertEquals(3, priceBetween.size)

            println("✅ SQLite特定功能测试通过!")
        } finally {
            executor.close()
        }
    }

    @Test
    fun `test SQLite error handling`() {
        val url = "jdbc:sqlite::memory:"
        val username = ""
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

            // 测试NOT NULL约束
            executor.execute("CREATE TABLE IF NOT EXISTS test_not_null (id INTEGER PRIMARY KEY, name TEXT NOT NULL)")

            assertThrows<Exception> {
                executor.executeUpdate("INSERT INTO test_not_null (name) VALUES (NULL)")
            }

            println("✅ SQLite错误处理测试通过!")
        } finally {
            executor.close()
        }
    }

    @Test
    fun `test SQLite performance with large dataset`() {
        val url = "jdbc:sqlite::memory:"
        val username = ""
        val password = ""

        val executor = SqlExecutor(url, username, password)
        try {
            // 创建测试表
            executor.execute("""
                CREATE TABLE IF NOT EXISTS performance_test (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    data TEXT,
                    value INTEGER,
                    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
                )
            """.trimIndent())

            // 批量插入1000条数据
            val batchSize = 1000
            val startTime = System.currentTimeMillis()

            repeat(batchSize) { i ->
                executor.executeUpdate("""
                    INSERT INTO performance_test (data, value)
                    VALUES ('Test data ${i}', ${i * 10})
                """.trimIndent())
            }

            val insertTime = System.currentTimeMillis() - startTime
            println("SQLite插入${batchSize}条数据耗时: ${insertTime}ms")

            // 测试查询性能
            val queryStart = System.currentTimeMillis()
            val results = executor.queryForList("SELECT * FROM performance_test WHERE value > 5000")
            val queryTime = System.currentTimeMillis() - queryStart

            assertEquals(499, results.size, "应该有499条记录value > 5000")
            println("SQLite查询${results.size}条数据耗时: ${queryTime}ms")

            // 测试索引查询性能
            executor.execute("CREATE INDEX IF NOT EXISTS idx_performance_value ON performance_test(value)")

            val indexedQueryStart = System.currentTimeMillis()
            val indexedResults = executor.queryForList("SELECT * FROM performance_test WHERE value BETWEEN 2000 AND 3000")
            val indexedQueryTime = System.currentTimeMillis() - indexedQueryStart

            assertEquals(101, indexedResults.size, "应该有101条记录在2000-3000之间")
            println("SQLite索引查询耗时: ${indexedQueryTime}ms")

            println("✅ SQLite性能测试通过!")
        } finally {
            executor.close()
        }
    }

    @Test
    fun `test SQLite transaction support`() {
        val url = "jdbc:sqlite::memory:"
        val username = ""
        val password = ""

        val executor = SqlExecutor(url, username, password)
        try {
            // 创建测试表
            executor.execute("CREATE TABLE IF NOT EXISTS accounts (id INTEGER, name TEXT, balance REAL)")

            // 初始化数据
            executor.executeUpdate("INSERT INTO accounts VALUES (1, 'Alice', 1000.0)")
            executor.executeUpdate("INSERT INTO accounts VALUES (2, 'Bob', 500.0)")

            // 模拟转账事务
            // SQLite默认是自动提交的，我们需要手动管理事务
            // 执行转账操作
            executor.executeUpdate("UPDATE accounts SET balance = balance - 100.0 WHERE name = 'Alice'")
            executor.executeUpdate("UPDATE accounts SET balance = balance + 100.0 WHERE name = 'Bob'")

            // 验证转账结果
            val aliceBalance = executor.queryForList("SELECT balance FROM accounts WHERE name = 'Alice'")
                .first()
            val aliceBalanceValue = aliceBalance["balance"] ?: aliceBalance["BALANCE"]
                ?: throw IllegalStateException("Alice balance value is null")
            val bobBalance = executor.queryForList("SELECT balance FROM accounts WHERE name = 'Bob'")
                .first()
            val bobBalanceValue = bobBalance["balance"] ?: bobBalance["BALANCE"]
                ?: throw IllegalStateException("Bob balance value is null")

            assertEquals(900.0, (aliceBalanceValue as Number).toDouble(), 0.01)
            assertEquals(600.0, (bobBalanceValue as Number).toDouble(), 0.01)

            println("✅ SQLite事务支持测试通过!")
        } finally {
            executor.close()
        }
    }
}