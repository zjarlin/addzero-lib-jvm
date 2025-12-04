# Tool-JVM 工具库

这是一个为 JVM 平台提供的通用工具库集合，包含了各种实用工具模块，旨在简化日常开发任务。

## 模块概览

### 网络工具
- [netty-util](netty-util/) - 基于 Netty 的简单易用的网络通信工具类
- [network-call](network-call/) - 网络调用相关工具
  - [tool-api-maven](network-call/tool-api-maven/) - Maven API 工具
  - [tool-api-tyc](network-call/tool-api-tyc/) - 天眼查 API 工具
  - [tool-api-tyc-hw](network-call/tool-api-tyc-hw/) - 天眼查华为相关 API 工具
  - [tool-api-weather](network-call/tool-api-weather/) - 天气 API 工具

### 数据库工具
- [database](database/) - 数据库相关工具集合
  - [mybatis-auto-wrapper](database/mybatis-auto-wrapper/) - MyBatis 自动包装器
  - [mybatis-auto-wrapper-core](database/mybatis-auto-wrapper-core/) - MyBatis 自动包装器核心
  - [tool-mybatis](database/tool-mybatis/) - MyBatis 工具
  - [tool-database-model](database/tool-database-model/) - 数据库模型工具
  - [tool-ddlgenerator](database/tool-ddlgenerator/) - DDL 生成器
  - [tool-cte](database/tool-cte/) - CTE 工具
  - [tool-sql-executor](database/tool-sql-executor/) - SQL 执行器

### Jimmer 工具
- [jimmer](jimmer/) - Jimmer 相关工具集合
  - [jimmer-ext-dynamic-datasource](jimmer/jimmer-ext-dynamic-datasource/) - 动态数据源扩展
  - [jimmer-ext-lowquery](jimmer/jimmer-ext-lowquery/) - LowQuery 扩展
  - [jimmer-model-lowquery](jimmer/jimmer-model-lowquery/) - LowQuery 模型

### 常用工具模块
- [tool-common-jvm](tool-common-jvm/) - JVM 通用工具
- [tool-log](tool-log/) - 日志工具
- [tool-excel](tool-excel/) - Excel 处理工具
- [tool-io](tool-io/) - IO 工具
- [tool-context](tool-context/) - 上下文工具
- [tool-area](tool-area/) - 地区工具
- [tool-jsr](tool-jsr/) - JSR 规范验证工具
- [tool-curl](tool-curl/) - Curl 工具
- [tool-docker](tool-docker/) - Docker 工具
- [tool-email](tool-email/) - 邮件工具
- [tool-funbox](tool-funbox/) - 函数工具箱
- [tool-math](tool-math/) - 数学工具
- [tool-pinyin](tool-pinyin/) - 拼音工具
- [tool-reflection](tool-reflection/) - 反射工具
- [tool-spel](tool-spel/) - SpEL 表达式工具
- [tool-spring](tool-spring/) - Spring 工具
- [tool-ssh](tool-ssh/) - SSH 工具
- [tool-toml](tool-toml/) - TOML 解析工具
- [tool-yml](tool-yml/) - YAML 工具

### 特殊用途工具
- [stream-wrapper](stream-wrapper/) - 流包装器
- [tool-cli-repl](tool-cli-repl/) - CLI REPL 工具
- [tool-mybatis-generator](tool-mybatis-generator/) - MyBatis 代码生成器
- [tool-ai](tool-ai/) - AI 相关工具
- [tool-api-jvm](tool-api-jvm/) - JVM API 工具
- [tool-io-codegen](tool-io-codegen/) - IO 代码生成工具
- [tool-jvmstr](tool-jvmstr/) - JVM 字符串工具
- [tool-spctx](tool-spctx/) - Spring 上下文工具

## 使用方式

每个工具模块都可以独立使用，具体使用方式请参考各模块下的 README 文档。

一般来说，在你的 `build.gradle.kts` 文件中添加相应依赖即可：

```kotlin
dependencies {
    implementation("site.addzero:module-name:version")
}
```

例如，要使用 netty-util 模块：

```kotlin
dependencies {
    implementation("site.addzero:netty-util:1.0.0")
}
```
