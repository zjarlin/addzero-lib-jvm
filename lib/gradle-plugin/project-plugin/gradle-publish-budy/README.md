# Gradle Publish Buddy

用于统一 Maven Central 发布配置，并增强多模块发布体验：

- 单模块发布时，自动递归发布 `project(...)` 依赖模块
- 自动为同一父目录下的叶子模块生成聚合发布任务

## 功能说明

### 1. 统一发布约定
插件内部基于 `com.vanniktech.maven.publish`，自动配置：

- `publishToMavenCentral(automaticRelease = true)`
- POM 基本信息（name / description / license / developers / scm）
- 检测到签名配置时自动 `signAllPublications()`

### 2. 递归发布 project 依赖
当执行某个模块的 `publishToMavenCentral` 时，会自动递归查找当前模块的 `ProjectDependency`（`project("...")` 依赖），并将这些依赖模块的发布任务加入依赖链。

示例：

- `B` 依赖 `A`
- `A` 依赖 `C`

执行 `B:publishToMavenCentral` 时，会自动先执行 `A/C` 的 `publishToMavenCentral`（若对应任务存在）。

### 3. 自动聚合发布任务
插件会按「父路径」自动生成聚合任务（在 root project 生效）：

- 规范任务名：`publish<完整路径Pascal>ToMavenCentral`
  - 例如 `:checkouts:lsi:*` => `publishCheckoutsLsiToMavenCentral`
- 短别名任务（当末段唯一时）：`publish<末段Pascal>ToMavenCentral`
  - 例如 `publishLsiToMavenCentral`

## 使用方法

### 1) 在根项目启用插件

```kotlin
plugins {
    id("site.addzero.gradle.plugin.publish-buddy") version "+"
}
```

> 建议在 root 应用，这样才能生成全局聚合任务。

### 2) 按需应用到要发布的子模块

例如仅对 `:checkouts:` 下模块启用：

```kotlin
subprojects {
    if (path.startsWith(":checkouts:")) {
        apply(plugin = "site.addzero.gradle.plugin.publish-buddy")
    }
}
```

### 3) 可选：覆盖发布元信息

```kotlin
import site.addzero.gradle.PublishConventionExtension

extensions.configure<PublishConventionExtension> {
    projectDescription.set("My awesome module")
    authorName.set("your-name")
    gitUrl.set("https://github.com/your-org/your-repo.git")
    emailDomain.set("example.com")
    licenseName.set("The Apache License, Version 2.0")
    licenseUrl.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
    licenseDistribution.set("repo")
}
```

## 常用命令

### 发布单个模块（会递归带上 project 依赖）

```bash
./gradlew :checkouts:lsi:lsi-ksp:publishToMavenCentral
```

### 发布某一目录下全部叶子模块

```bash
./gradlew publishCheckoutsLsiToMavenCentral
# 或（末段唯一时）
./gradlew publishLsiToMavenCentral
```

### 查看自动生成的发布任务

```bash
./gradlew tasks --group publishing
```

## 注意事项

1. 递归发布只处理 `project(...)` 依赖，不处理外部 Maven 依赖。
2. 若依赖模块不存在 `publishToMavenCentral` 任务，会被自动跳过。
3. 发布到 Maven Central 仍需你准备好账号、凭据和签名配置。
