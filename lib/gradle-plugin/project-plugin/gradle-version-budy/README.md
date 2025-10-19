# Gradle Version Buddy

一个Gradle插件，用于自动化管理项目版本号。


在 `build.gradle.kts` 中：

```kotlin
plugins {
    id("site.addzero.buildlogic.version-buddy") version "+"
}
```


1. **gradle.properties 配置**：如果在 `gradle.properties` 文件中配置了 `version` 属性，则使用该值
   ```properties
   version=1.2.3
   ```

2. **项目当前版本**：如果项目已有版本号（非 "unspecified"），则使用该项目版本


### 3. 版本号递增规则
```kotlin
// 获取默认版本的逻辑：
// 1. 如果gradle.properties里配置了version属性，就用配置的
// 2. 如果project.version有值，就用project.version的值
// 3. 如果都沒有，就用找一下maven中央仓库的最新版本,然后nextVersion  ; 4. 如果都沒有，就用今天的日期格式版本例如: 2025.01.01

val configuredVersion = findProperty("version") as String?
val projectVersion = project.version.toString().takeIf { it != "unspecified" }
val defaultVersion = configuredVersion ?: projectVersion ?: ""
subprojects {
    if (defaultVersion.isNotBlank()) {
        version = defaultVersion
        return@subprojects
    }

    val group = project.group.toString()
    if (group.isBlank()) {
        error("auto version error, you must set group")
    }

    val latestVersion = MavenUtil.getLatestVersion(group, projectDir.name)
    if (latestVersion.isNullOrBlank()) {
        version = VersionUtil.defaultVersion()
    }
    println("Latest version for ${project.name}: $latestVersion")
    val nextVersion = VersionUtil.nextVersion(latestVersion)
    println("Next version for ${project.name}: $nextVersion")
    version = nextVersion

}

```

## 注意事项
插件会自动应用到所有子项目,maven最新版本的查询是单个查询,而非批量查询(有人我怎么批量查询我可以改进),会拖慢构建速度
