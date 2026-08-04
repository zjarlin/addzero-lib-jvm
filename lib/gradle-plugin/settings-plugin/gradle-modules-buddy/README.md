# gradle-modules-buddy

Gradle Settings 插件，用于自动发现 Kotlin DSL 子模块和复合构建。

## 快速开始

```kotlin
// settings.gradle.kts
plugins {
  id("site.addzero.gradle.plugin.modules-buddy") version "2026.08.03"
}

autoModules {}
```

Maven 坐标：`site.addzero:gradle-modules-buddy:2026.08.03`

## 扫描边界

| 场景 | 行为 |
|---|---|
| 普通目录包含 `build.gradle.kts` | 自动加入 Gradle build |
| `build-logic*` 目录 | 作为复合构建加入 |
| 隐藏目录 | 不进入目录树 |
| `build`、`node_modules`、`out`、`target` | 不进入目录树 |
| `autoModules.excludeModules` 命中 | 保留在跳过列表，不加入 build |

在递归入口排除生成目录，避免 Gradle Configuration Cache 将自身文件记录为配置输入并持续生成大型报告。
