# Gradle Git Dependency Plugin

简化 Git 仓库检出配置的 Gradle Settings 插件，**支持同时混用 Gitee 和 GitHub 仓库**。

参考 https://melix.github.io/includegit-gradle-plugin/latest/index.html#_known_limitations

## 功能

- 全局默认值 + 每个仓库独立覆盖（repoType / author / branch / checkoutDir）
- 同一个项目中可混合使用 Gitee 和 GitHub 仓库
- 兼容旧版 `remoteGits` 列表写法

## 使用方法

在 `settings.gradle.kts` 中：

```kotlin
plugins {
    id("site.addzero.gradle.plugin.addzero-git-dependency") version "+"
}

implementationRemoteGit {
    // 全局默认值（可选，以下为默认值）
    repoType = RepoType.GITEE
    author = "zjarlin"
    branch = "master"

    // 简单写法 — 继承全局默认值
    repo("my-gitee-lib")

    // 独立覆盖 — 这个仓库从 GitHub 拉取
    repo("my-github-lib") {
        repoType = RepoType.GITHUB
        author = "other-user"
        branch = "main"
    }

    // 旧版写法仍然兼容（全部使用全局默认值）
    // remoteGits.set(listOf("lib-a", "lib-b"))
}
```

## DSL 说明

### `repo(name: String)`
添加一个仓库，使用全局默认值。

### `repo(name: String) { ... }`
添加一个仓库，在闭包中覆盖任意字段：
- `repoType` — `RepoType.GITEE` 或 `RepoType.GITHUB`
- `author` — 仓库所有者
- `branch` — 分支名
- `checkoutDir` — 本地检出目录

### `remoteGits`（旧版兼容）
`ListProperty<String>`，所有仓库名统一使用全局默认值。

## 依赖

- `me.champeau.includegit:me.champeau.includegit.gradle.plugin`

