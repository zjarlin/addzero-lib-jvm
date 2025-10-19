# Gradle Version Buddy

一个Gradle插件，用于自动化管理项目版本号。

在 `build.gradle.kts` 中：

```kotlin
plugins {
    //已知的稳定最新版本为0.0.647
    id("site.addzero.buildlogic.version-buddy") version "+"
}
```

1. **gradle.properties 配置**：如果在 `gradle.properties` 文件中配置了 `version` 属性，则使用该值
   ```properties
   version=1.2.3
   ```
2. **项目当前版本**：如果项目已有版本号（非 "unspecified"），则使用该项目版本

### 3. 版本号递增规则

1. 如果gradle.properties里配置了version属性，就用配置的+1
2. 如果project.version有值，就用project.version的值+1
3. 如果都沒有，就用找一下maven中央仓库的最新版本,然后nextVersion ;
4. 如果都沒有，就用今天的日期格式版本例如: 2025.01.01

## 注意事项

插件会自动应用到所有子项目,maven最新版本的查询是单个查询,而非批量查询(有人我怎么批量查询我可以改进),会拖慢构建速度
latestversion 可能会有延迟,so nextVersion亦有延迟,如果你发版太快,会出现不自洽的情况!!!  那么这时候应当手动控制版本(
那么nextVersion读取程序中现有的版本肯定不会有延迟问题),例如:

```kotlin
subprojects {
    version = "0.0.647"
}

```
