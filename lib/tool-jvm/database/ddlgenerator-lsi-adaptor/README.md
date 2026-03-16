# ddlgenerator-lsi-adaptor

- 作用：把 `site.addzero.lsi.*` + Jimmer/JPA 注解语义转换为 `AutoDdlSchema`。
- Maven 坐标：`site.addzero:ddlgenerator-lsi-adaptor`
- 本地路径：`lib/tool-jvm/database/ddlgenerator-lsi-adaptor`
- 约束：只做语义适配，不读取数据库、不生成 SQL。

```kotlin
val schema = LsiAutoDdlSchemaAdapter.from(lsiClasses)
```
