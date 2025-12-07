# DDL Generator Koin 集成

## 概述

此模块负责整合所有 DDL Generator 的方言实现，使用 Koin 依赖注入自动扫描和注册。

## 功能

- 📦 **自动扫描** - 扫描所有带 `@Single` 注解的方言实现
- 🔌 **自动注册** - 将方言注册到 `DdlDialectRegistry`
- 🎯 **统一入口** - 提供 DSL 和便捷函数

## 使用方式

### 初始化

在应用启动时调用一次：

```kotlin
import org.koin.core.context.startKoin
import site.addzero.util.ddlgenerator.koin.DdlGeneratorKoinInitializer

// 1. 启动 Koin
startKoin {
    // 你的其他配置
}

// 2. 初始化 DDL Generator
DdlGeneratorKoinInitializer.initialize()
```

### 使用生成器

```kotlin
import site.addzero.util.db.DatabaseType
import site.addzero.util.ddlgenerator.koin.*

// 方式 1: 使用便捷函数
val generator = ddlGenerator(DatabaseType.MYSQL)
val sql = generator.generateCreateTable(lsiClass)

// 方式 2: 使用扩展函数
val sql = lsiClass.toCreateTableSql(DatabaseType.MYSQL)

// 方式 3: 使用 DSL
val sql = generateDdl(DatabaseType.POSTGRESQL) {
    generateCreateTable(lsiClass)
}
```

## 依赖

此模块自动依赖所有方言实现：

- `tool-ddlgenerator-core`
- `tool-ddlgenerator-dialect-mysql`
- `tool-ddlgenerator-dialect-postgresql`
- ...其他方言模块

## 工作原理

1. **ComponentScan** - Koin 扫描 `site.addzero.util.ddlgenerator.dialect` 包
2. **自动实例化** - 所有带 `@Single` 注解的方言类被实例化
3. **注册** - 将实例注册到 `DdlDialectRegistry`
4. **使用** - 通过 `DatabaseType` 查询对应的方言

## 添加新方言

只需：
1. 实现 `DdlDialect` 接口
2. 添加 `@Single` 注解
3. 将新模块添加到 koin 模块的依赖

就会自动被扫描和注册！

---

**作者**: Droid (Factory AI)  
**日期**: 2025-12-07
