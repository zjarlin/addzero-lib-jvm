# H2 内存数据库测试指南

## 📝 概述

本项目提供了完整的 H2 内存数据库测试示例，展示如何使用 `SqlExecutor` 执行任意 SQL 语句。

## 🚀 快速开始

### 配置 H2 数据库

```yaml
spring:
  application:
    name: store-service
  datasource:
    driver-class-name: org.h2.Driver
    url: jdbc:h2:mem:store-db;database_to_upper=true
    username: sa
    password: ""
```

### 基础使用

```kotlin
SqlExecutor("jdbc:h2:mem:test-db", "sa", "").use { executor ->
    // 执行任意 SQL
    executor.execute("CREATE TABLE users (id INT PRIMARY KEY, name VARCHAR(50))")
    executor.executeUpdate("INSERT INTO users VALUES (1, 'Alice')")
    val users = executor.queryForList("SELECT * FROM users")
}
```

## 🧪 测试覆盖

### 1. 完整 CRUD 测试

**测试方法**: `test H2 in-memory database with arbitrary SQL()`

涵盖功能：
- ✅ **CREATE TABLE** - 创建产品表、订单表、日志表
- ✅ **INSERT** - 批量插入数据（函数式写法）
- ✅ **SELECT** - 基础查询、条件查询、聚合查询
- ✅ **UPDATE** - 更新库存数量
- ✅ **DELETE** - 删除指定记录
- ✅ **JOIN** - 多表关联查询
- ✅ **FOREIGN KEY** - 外键约束

**代码示例**：
```kotlin
// 函数式批量插入
listOf(
    "INSERT INTO products (name, price, stock) VALUES ('Laptop', 5999.99, 10)",
    "INSERT INTO products (name, price, stock) VALUES ('Mouse', 99.99, 50)",
    "INSERT INTO products (name, price, stock) VALUES ('Keyboard', 299.99, 30)"
).forEach(executor::execute)

// 流式查询和验证
executor.queryForList("SELECT * FROM products WHERE price > 100")
    .also { products ->
        assertEquals(2, products.size)
        assertTrue(products.all { 
            (it["PRICE"] as? Number)?.toDouble()?.let { p -> p > 100 } == true 
        })
    }

// 聚合查询
executor.queryForList("SELECT COUNT(*) as total, SUM(stock) as total_stock FROM products")
    .first()
    .also { result ->
        assertEquals(3, (result["TOTAL"] as Number).toInt())
        assertEquals(90, (result["TOTAL_STOCK"] as Number).toInt())
    }
```

### 2. DDL 操作测试

**测试方法**: `test H2 with DDL operations()`

涵盖功能：
- ✅ **多表创建** - users, user_profiles
- ✅ **ALTER TABLE** - 动态添加列
- ✅ **LEFT JOIN** - 外连接查询
- ✅ **DROP TABLE** - 删除表（注意外键顺序）

**代码示例**：
```kotlin
// 创建关联表
executor.execute("""
    CREATE TABLE users (
        id BIGINT PRIMARY KEY,
        username VARCHAR(50) UNIQUE NOT NULL,
        email VARCHAR(100)
    )
""".trimIndent())

// 动态修改表结构
executor.execute("ALTER TABLE users ADD COLUMN created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP")

// 关联查询
executor.queryForList("""
    SELECT u.username, u.email, p.bio
    FROM users u
    LEFT JOIN user_profiles p ON u.id = p.user_id
""".trimIndent())
```

### 3. 自定义函数测试

**测试方法**: `test H2 with stored procedures and functions()`

涵盖功能：
- ✅ **CREATE ALIAS** - 创建 Java 函数别名
- ✅ **自定义函数调用** - 在 SQL 中使用

**代码示例**：
```kotlin
// H2 特有功能：创建 Java 函数别名
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
```

## 🎨 编码风格

### 函数式风格
```kotlin
// ✅ 推荐：函数式链式调用
executor.queryForList("SELECT * FROM products")
    .also { products -> 
        products.forEach { println("产品: $it") }
    }

// ✅ 推荐：stream流处理
listOf("sql1", "sql2", "sql3").forEach(executor::execute)
```

### 资源管理
```kotlin
// ✅ 推荐：使用 use 扩展函数自动关闭资源
SqlExecutor(url, username, password).use { executor ->
    // 执行 SQL 操作
}

// ❌ 不推荐：手动 try-finally
val executor = SqlExecutor(url, username, password)
try {
    // ...
} finally {
    executor.close()
}
```

### 断言风格
```kotlin
// ✅ 推荐：also 链式断言
executor.queryForList("SELECT COUNT(*) as total FROM products")
    .first()["TOTAL"]
    .also { total -> 
        assertEquals(3, (total as Number).toInt(), "产品总数应为3")
    }
```

## 📊 测试数据模型

### 产品表 (products)
```sql
CREATE TABLE products (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    stock INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
)
```

**测试数据**：
- Laptop: ¥5999.99, 库存 10
- Mouse: ¥99.99, 库存 50
- Keyboard: ¥299.99, 库存 30

### 订单表 (orders)
```sql
CREATE TABLE orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT,
    quantity INT,
    FOREIGN KEY (product_id) REFERENCES products(id)
)
```

## 🔧 运行测试

```bash
# 运行所有 H2 测试
./gradlew :lib:tool-jvm:database:tool-sql-executor:test --tests "*H2*"

# 运行单个测试
./gradlew :lib:tool-jvm:database:tool-sql-executor:test \
  --tests "site.addzero.util.db.SqlExecutorTest.test H2 in-memory database with arbitrary SQL"
```

## ⚠️ 注意事项

### 1. H2 数据类型映射
```kotlin
// H2 返回 Integer/Long，需要统一转换
val count = (result["COUNT"] as Number).toInt()  // ✅
val count = result["COUNT"] as Long              // ❌ 可能失败
```

### 2. 外键约束顺序
```kotlin
// ✅ 正确：先创建订单再删除产品
executor.execute("INSERT INTO orders ...")
executor.executeUpdate("DELETE FROM products WHERE id = 3")

// ❌ 错误：先删除产品会导致外键约束冲突
executor.executeUpdate("DELETE FROM products WHERE id = 2")
executor.execute("INSERT INTO orders (product_id) VALUES (2)")
```

### 3. 大小写敏感性
```kotlin
// H2 配置: database_to_upper=true
// 列名会自动转换为大写
result["NAME"]   // ✅
result["name"]   // ❌ 可能返回 null
```

## 🎯 实战示例

### 完整的电商场景测试
```kotlin
@Test
fun `test e-commerce scenario`() {
    SqlExecutor("jdbc:h2:mem:ecommerce", "sa", "").use { executor ->
        // 1. 初始化数据库
        executor.execute("""
            CREATE TABLE products (
                id INT PRIMARY KEY,
                name VARCHAR(100),
                price DECIMAL(10,2),
                stock INT
            )
        """.trimIndent())
        
        // 2. 导入商品数据（函数式）
        listOf(
            Triple(1, "iPhone 15", 5999.00),
            Triple(2, "MacBook Pro", 12999.00),
            Triple(3, "AirPods", 1299.00)
        ).forEach { (id, name, price) ->
            executor.execute("INSERT INTO products VALUES ($id, '$name', $price, 100)")
        }
        
        // 3. 模拟购买（减库存）
        executor.executeUpdate("UPDATE products SET stock = stock - 1 WHERE id = 1")
        
        // 4. 查询热销商品（库存<100）
        executor.queryForList("""
            SELECT name, price, stock 
            FROM products 
            WHERE stock < 100 
            ORDER BY stock DESC
        """.trimIndent())
            .also { hotProducts ->
                assertTrue(hotProducts.any { it["NAME"] == "iPhone 15" })
            }
    }
}
```

## 📚 扩展阅读

- [H2 Database 官方文档](http://www.h2database.com/)
- [SqlExecutor API 文档](./src/main/kotlin/site/addzero/util/db/SqlExecutor.kt)
- [JUnit 5 测试指南](https://junit.org/junit5/docs/current/user-guide/)

---

**测试结果**: ✅ 所有测试通过
- `test H2 in-memory database with arbitrary SQL()` ✅
- `test H2 with DDL operations()` ✅
- `test H2 with stored procedures and functions()` ✅
