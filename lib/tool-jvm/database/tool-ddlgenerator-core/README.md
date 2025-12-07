# DDL Generator - 多模块架构

## 概述

DDL Generator 采用多模块架构，基于 Koin 依赖注入和策略模式，支持多种数据库方言的 DDL 生成。

## 模块架构

```
tool-ddlgenerator/
├── tool-ddlgenerator-core/                 # 核心接口和模型（无外部依赖）
├── tool-ddlgenerator-dialect-mysql/        # MySQL 方言实现
├── tool-ddlgenerator-dialect-postgresql/   # PostgreSQL 方言实现
└── tool-ddlgenerator-koin/                 # Koin 集成（自动注册策略）
```

### 核心特性

- ✅ **面向 LSI 接口编程** - 基于语言无关的结构抽象
- ✅ **策略模式** - 每个数据库方言独立实现
- ✅ **Koin 注解** - 自动扫描和注册方言
- ✅ **多模块** - 清晰的职责分离
- ✅ **易扩展** - 添加新方言只需实现接口并加 `@Single` 注解

## 使用方式

### 基础用法

```kotlin
import site.addzero.util.db.DatabaseType
import site.addzero.util.ddlgenerator.koin.ddlGenerator
import site.addzero.util.lsi.clazz.LsiClass

// 1. 获取 LsiClass（通过 PSI、Kotlin 或反射）
val lsiClass: LsiClass = // ...

// 2. 创建生成器
val generator = ddlGenerator(DatabaseType.MYSQL)

// 3. 生成 DDL
val createTableSql = generator.generateCreateTable(lsiClass)
println(createTableSql)
```

### 使用 DSL

```kotlin
import site.addzero.util.ddlgenerator.koin.*

// 生成 CREATE TABLE
val sql = lsiClass.toCreateTableSql(DatabaseType.MYSQL)

// 生成完整 schema
val schemaSql = listOf(class1, class2, class3).toSchemaSql(DatabaseType.POSTGRESQL)

// 使用 DSL 块
val ddl = generateDdl(DatabaseType.MYSQL) {
    generateCreateTable(lsiClass)
}
```

### 初始化

在应用启动时初始化 Koin 和方言注册：

```kotlin
import org.koin.core.context.startKoin
import site.addzero.util.ddlgenerator.koin.DdlGeneratorKoinInitializer

// 1. 启动 Koin
startKoin {
    // 你的 Koin 配置
}

// 2. 初始化 DDL Generator（自动注册所有方言）
DdlGeneratorKoinInitializer.initialize()

// 现在可以使用生成器了
```

## 支持的数据库

当前支持的数据库：

- ✅ MySQL
- ✅ PostgreSQL
- ⬜ Oracle（待实现）
- ⬜ SQL Server（待实现）
- ⬜ H2（待实现）

## 添加新的数据库方言

### 1. 创建新模块

```bash
mkdir -p lib/tool-jvm/database/tool-ddlgenerator-dialect-oracle/src/main/kotlin/site/addzero/util/ddlgenerator/dialect/oracle
```

### 2. 实现方言接口

```kotlin
package site.addzero.util.ddlgenerator.dialect.oracle

import org.koin.core.annotation.Single
import site.addzero.util.db.DatabaseType
import site.addzero.util.ddlgenerator.core.DdlDialect
import site.addzero.util.lsi.clazz.LsiClass
import site.addzero.util.lsi.field.LsiField
import site.addzero.util.lsi.database.*

@Single  // Koin 注解，自动注册
class OracleDdlDialect : DdlDialect {
    
    override val databaseType = DatabaseType.ORACLE
    
    override fun generateCreateTable(lsiClass: LsiClass): String {
        // 实现 Oracle CREATE TABLE 逻辑
    }
    
    override fun getColumnTypeName(
        columnType: DatabaseColumnType,
        precision: Int?,
        scale: Int?
    ): String {
        return when (columnType) {
            DatabaseColumnType.INT -> "NUMBER(10)"
            DatabaseColumnType.BIGINT -> "NUMBER(19)"
            DatabaseColumnType.VARCHAR -> "VARCHAR2(${precision ?: 255})"
            // ... 其他类型映射
        }
    }
    
    // 实现其他必要方法
}
```

### 3. 创建 build.gradle.kts

```kotlin
plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention")
    id("com.google.devtools.ksp")
}

dependencies {
    implementation(project(":lib:tool-jvm:database:tool-ddlgenerator-core"))
    implementation("io.insert-koin:koin-annotations:1.3.1")
    ksp("io.insert-koin:koin-ksp-compiler:1.3.1")
}
```

### 4. 在 settings.gradle.kts 中包含模块

```kotlin
include(":lib:tool-jvm:database:tool-ddlgenerator-dialect-oracle")
```

### 5. 在 koin 模块中引用

```kotlin
// tool-ddlgenerator-koin/build.gradle.kts
dependencies {
    api(project(":lib:tool-jvm:database:tool-ddlgenerator-dialect-oracle"))
}
```

完成！重新构建后，新方言会自动被扫描和注册。

## 设计原则

1. **关注点分离** - 核心接口、方言实现、Koin 集成分离
2. **面向接口** - 基于 LSI 抽象层和 DdlDialect 接口
3. **零依赖核心** - core 模块只依赖 LSI 和 database-model
4. **策略模式** - 每个数据库方言独立实现
5. **依赖注入** - 使用 Koin 自动管理方言实例
6. **易于扩展** - 添加新方言只需实现接口并加注解

## 架构优势

### vs 旧实现

| 方面 | 旧实现 | 新实现 |
|------|--------|--------|
| 架构 | 单模块，耦合严重 | 多模块，职责清晰 |
| 方言管理 | 硬编码工厂 | Koin 自动注册 |
| 扩展性 | 需修改多处代码 | 只需添加新模块 |
| 依赖 | 混乱 | 清晰的分层依赖 |
| 测试 | 困难 | 每个模块独立测试 |

## 示例

查看 `tool-ddlgenerator-dialect-mysql` 和 `tool-ddlgenerator-dialect-postgresql` 作为参考实现。

## 贡献

欢迎贡献新的数据库方言实现！

---

**作者**: Droid (Factory AI)  
**日期**: 2025-12-07
