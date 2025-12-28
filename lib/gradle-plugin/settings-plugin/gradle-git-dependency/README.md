# Gradle Checkout Repos Plugin

这是一个简化Git仓库检出配置的Gradle预编译脚本。
参考 https://melix.github.io/includegit-gradle-plugin/latest/index.html#_known_limitations


## 功能

- 简化Git仓库的检出配置
- 支持GitHub和Gitee仓库
- 默认作者为zjarlin
- 默认分支为master
- 只需要提供项目名即可使用

## 使用方法

在您的settings.gradle.kts文件中：

```kotlin
// 导入预编译脚本
apply(from = "https://raw.githubusercontent.com/zjarlin/addzero-lib-jvm/master/lib/gradle-plugin/settings-plugin/gradle-checkout-repos/src/main/resources/META-INF/.settings.gradle.kts")

plugins {
    id("me.champeau.includegit") version "0.1.5"
}

// 然后就可以使用简化的方法了
gitRepositories {
    // 方式1：简化方式 - 只需要项目名（使用默认配置：GitHub, zjarlin, master）
    includeProjects("project-a", "project-b", "project-c")

    // 方式2：自定义方式
    includeProject("custom-project", RepoType.GITEE, "other-user", "develop")
}
```

## 方法说明

### 简化方法
- `includeProjects(vararg projectNames: String)` - 批量添加项目，使用默认配置

### 自定义方法
- `includeProject(projectName: String, repoType: RepoType = RepoType.GITHUB, owner: String = "zjarlin", branch: String = "master")` - 添加单个项目并自定义配置

## 依赖

- me.champeau.includegit:me.champeau.includegit.gradle.plugin:0.1.5

