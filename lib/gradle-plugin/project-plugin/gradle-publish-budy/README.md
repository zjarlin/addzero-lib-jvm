# gradle-publish-budy

> Gradle 预编译脚本插件，封装 [vanniktech/maven-publish](https://github.com/vanniktech/gradle-maven-publish-plugin) 并补全多模块发布所需的元数据、依赖排序与聚合任务。

**插件 ID**：`site.addzero.gradle.plugin.publish-buddy`

## 核心能力

### 1. 一键 Maven Central 发布

应用插件后自动配置：

- `publishToMavenCentral(automaticRelease = true)`
- POM 元数据（name / description / inceptionYear / url）
- License（默认 Apache 2.0）
- Developer 信息
- SCM 连接地址

### 2. README 自动摘要

自动读取模块目录下的 `README.md`，提取首段非空、非标题文本作为项目 `description`，省去手动维护。

### 3. 发布依赖排序

根据模块间 **project 依赖**（`api(project(...))`/ `implementation(project(...))` 等 consumable configuration）递归推导发布顺序，确保被依赖模块先发布。

- 只追踪 consumable configuration，`testImplementation` 等不会引入发布环
- 支持 typesafe project accessors（`projects.foo.bar`）

### 4. 聚合发布任务（可选）

开启 `enableAggregatePublishTasksByParentDir` 后，插件会按父目录分组生成聚合任务：

```
# 发布 :lib:ksp 下所有子模块
./gradlew publishLibKspToMavenCentral

# 短别名（父目录名唯一时可用）
./gradlew publishKspToMavenCentral
```

### 5. 自动向子模块传播

根项目应用插件后，所有 `:lib` 开头的子项目会自动 apply 同一插件，无需逐个声明。

## 使用方式

### 根项目

```kotlin
// build.gradle.kts
plugins {
    alias(libs.plugins.site.addzero.gradle.plugin.publish.buddy)
}

// 可选：覆盖默认配置
import site.addzero.gradle.PublishConventionExtension

configure<PublishConventionExtension> {
    projectDescription.set("自定义描述")
    authorName.set("your-name")
    gitUrl.set("https://github.com/org/repo.git")
    emailDomain.set("example.com")
    useLeafName.set(false)           // artifactId 使用 root-to-leaf 拼接，如 myrepo-auth
    enableAggregatePublishTasksByParentDir.set(true)
}
```

### 子项目（非 :lib 路径或独立使用）

```kotlin
plugins {
    id("site.addzero.gradle.plugin.publish-buddy")
}
```

## 扩展属性一览

| 属性 | 类型 | 默认值 | 说明 |
|---|---|---|---|
| `projectDescription` | `Property<String>` | README 首段 / 项目 description / 兜底文案 | POM description |
| `authorName` | `Property<String>` | `zjarlin` | Developer ID & Name |
| `gitUrl` | `Property<String>` | 仓库 Git 地址 | 用于 SCM & URL |
| `emailDomain` | `Property<String>` | `outlook.com` | 拼接 author email |
| `useLeafName` | `Property<Boolean>` | `true`（继承父级） | `true` = artifactId 取项目名；`false` = root-to-leaf 拼接 |
| `enableAggregatePublishTasksByParentDir` | `Property<Boolean>` | `false` | 是否生成按父目录分组的聚合 publish 任务 |
| `licenseName` | `Property<String>` | Apache 2.0 | License 名称 |
| `licenseUrl` | `Property<String>` | Apache 2.0 URL | License 链接 |
| `licenseDistribution` | `Property<String>` | 同 licenseUrl | POM distribution 字段 |

## artifactId 命名规则

- **`useLeafName = true`**（默认）：artifactId = 子项目名，如 `tool-jackson`
- **`useLeafName = false`**：artifactId = `{rootProject.name}-{path segments}` 拼接，如 `system-auth`

## 签名

当检测到 `signing.keyId` / `signing.password` / `signing.secretKeyRingFile` 任一属性时自动启用 `signAllPublications()`，否则跳过签名。

## 依赖

- `com.vanniktech.maven.publish`（vanniktech Maven Publish 插件）
- `site.addzero.gradle.script.core`（内部工具库，提供 `createExtension` 等辅助方法）
