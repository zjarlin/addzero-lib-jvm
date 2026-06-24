# jimmer-ddl-compiler

`jimmer-ddl-compiler` 是 Jimmer 实体的编译期 DDL 生成器：

- Kotlin Jimmer `interface`：通过 KSP 收集 `@Entity`，再转成 LSI 元数据。
- Java Jimmer `interface`：通过 APT 收集 `@Entity`，并把零参数 getter/方法适配为 LSI 字段。
- DDL 输出：复用 `ddlgenerator`/`AutoDdlRuntime.generate(...)`，支持索引、外键、注释、序列、多对多中间表等开关。
- Flyway 格式：默认输出 `V<version>__<description>.sql`，可直接放入 `db/migration` 资源目录。

## 推荐用法：Gradle 插件

插件 id：

```kotlin
plugins {
    id("site.addzero.ksp.jimmer-ddl-compiler") version "<addzero-version>"
}

jimmerDdl {
    databaseType.set("postgresql")
    outputFormat.set("flyway")
    version.set("1001")
    description.set("jimmer_auto_ddl_generated")
    includePackages.set("com.example.domain")
}
```

插件会：

1. 对 Kotlin JVM 工程自动应用 KSP，并注入 `jimmer-ddl-compiler-processor`。
2. 对 Java 工程自动注入 `annotationProcessor`，所以 Java Jimmer 实体也能编译期产出 DDL。
3. 把 `build/generated/jimmer-ddl/main/resources` 加入 `main` resources，让 Flyway 可以从运行时 classpath 读取生成的迁移脚本。

## Raw KSP / APT 兜底用法

插件不可用时才建议手动接入：

```kotlin
dependencies {
    ksp("site.addzero:jimmer-ddl-compiler-processor:<addzero-version>")
    annotationProcessor("site.addzero:jimmer-ddl-compiler-processor:<addzero-version>")
}

ksp {
    arg("jimmerDdl.databaseType", "postgresql")
    arg("jimmerDdl.outputFormat", "flyway")
    arg("jimmerDdl.outputDir", layout.buildDirectory.dir("generated/jimmer-ddl/main/resources/db/migration").get().asFile.absolutePath)
    arg("jimmerDdl.includePackages", "com.example.domain")
}

tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs.addAll(
        listOf(
            "-AjimmerDdl.databaseType=postgresql",
            "-AjimmerDdl.outputFormat=flyway",
            "-AjimmerDdl.includePackages=com.example.domain",
        )
    )
}
```

## 编译参数

`processorBuddy.mustMap` 源定义在 `jimmer-ddl-compiler-processor/build.gradle.kts`，当前支持：

| Key | 默认值 | 说明 |
| --- | --- | --- |
| `jimmerDdl.enabled` | `true` | 是否启用生成。 |
| `jimmerDdl.profiles` | 空 | 逗号或分号分隔的多输出配置名；为空时使用单输出参数。 |
| `jimmerDdl.databaseType` | `postgresql` | 目标数据库，支持 `postgresql`、`mysql`、`h2` 等 `DatabaseType` 值；别名支持 `pg`、`postgres`、`mssql`、`dameng`。 |
| `jimmerDdl.outputFormat` | `flyway` | `flyway` 输出 `V版本__描述.sql`；`plain` 输出 `描述.sql`。 |
| `jimmerDdl.outputDir` | `build/generated/jimmer-ddl/main/resources/db/migration` | SQL 输出目录。插件模式下默认转为当前模块 `buildDir` 下的绝对路径。 |
| `jimmerDdl.version` | `1001` | Flyway 版本号。 |
| `jimmerDdl.description` | `jimmer_auto_ddl_generated` | Flyway 描述。非字母数字下划线会归一化为 `_`。 |
| `jimmerDdl.includePackages` | 空 | 逗号或分号分隔的实体包名前缀白名单；为空表示不过滤。 |
| `jimmerDdl.excludePackages` | 空 | 逗号或分号分隔的实体包名前缀黑名单；优先级高于 `includePackages`。 |
| `jimmerDdl.includeForeignKeys` | `true` | 是否生成外键。 |
| `jimmerDdl.includeIndexes` | `true` | 是否生成索引。 |
| `jimmerDdl.includeComments` | `true` | 是否生成注释。 |
| `jimmerDdl.includeSequences` | `true` | 是否生成序列。 |
| `jimmerDdl.includeManyToManyTables` | `true` | 是否生成多对多中间表。 |

兼容旧参数：`sqlSavePath` 等价于 `jimmerDdl.outputDir`，`dbType` 等价于 `jimmerDdl.databaseType`。

多输出配置使用 `jimmerDdl.profile.<name>.<key>` 覆盖单输出参数，例如：

```kotlin
ksp {
    arg("jimmerDdl.profiles", "prompt,knowledge")
    arg("jimmerDdl.profile.prompt.version", "1001")
    arg("jimmerDdl.profile.prompt.description", "prompt_generated")
    arg("jimmerDdl.profile.prompt.includePackages", "com.example.prompt.entity")
    arg("jimmerDdl.profile.knowledge.version", "1002")
    arg("jimmerDdl.profile.knowledge.description", "knowledge_generated")
    arg("jimmerDdl.profile.knowledge.includePackages", "com.example.knowledge.entity")
}
```

## 验证模块

- `jimmer-ddl-compiler-ksp-smoke`：Kotlin Jimmer 实体通过 KSP 生成 Flyway SQL。
- `jimmer-ddl-compiler-apt-smoke`：Java Jimmer 实体通过 APT 生成 Flyway SQL。

运行：

```bash
./gradlew --configure-on-demand \
  :lib:ksp:metadata:jimmer-ddl-compiler:jimmer-ddl-compiler-ksp-smoke:verifyGeneratedJimmerDdl \
  :lib:ksp:metadata:jimmer-ddl-compiler:jimmer-ddl-compiler-apt-smoke:verifyGeneratedJimmerDdl
```
