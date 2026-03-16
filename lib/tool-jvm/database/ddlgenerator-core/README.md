# ddlgenerator-core

- 作用：提供 AutoDDL 纯领域模型、schema diff planner、DDL operation 模型、方言 SPI。
- Maven 坐标：`site.addzero:ddlgenerator-core`
- 本地路径：`lib/tool-jvm/database/ddlgenerator-core`
- 约束：不依赖 LSI、JDBC reader、YAML、Settings 或 Koin。

```kotlin
val operations = SchemaDiffPlanner.plan(desiredSchema, actualSchema, AutoDdlDiffOptions())
val sql = AutoDdlDialects.require(DatabaseType.POSTGRESQL).render(operations)
```
