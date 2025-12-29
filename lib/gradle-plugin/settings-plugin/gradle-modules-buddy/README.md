# Gradle Modules Buddy (Gradle模块自动扫描插件)

自动扫描项目目录并包含所有 Gradle 子模块，无需手动配置 `include`。

## 功能

- 自动扫描并包含所有包含 `build.gradle.kts` 的子目录
- 支持排除指定模块
- 零配置开箱即用

## 使用方法

在 `settings.gradle.kts` 中应用插件：

```kotlin
plugins {
    id("site.addzero.modules-buddy") version "+"
}
```

插件会自动扫描并包含所有模块。

## 配置选项

### 排除特定模块

如果需要排除某些模块，可以配置 `excludeModules`：

```kotlin
autoModules {
    excludeModules.set(arrayOf("buildSrc", "build-logic", "checkouts"))
}
```

## 示例

### 项目结构

```
project/
├── settings.gradle.kts
├── build.gradle.kts
├── moduleA/
│   └── build.gradle.kts
├── moduleB/
│   └── build.gradle.kts
└── lib/
    ├── moduleC/
    │   └── build.gradle.kts
    └── moduleD/
        └── build.gradle.kts
```

### 基础用法

```kotlin
plugins {
    id("site.addzero.modules-buddy") version "+"
}
```

自动包含：
- `:moduleA`
- `:moduleB`
- `:lib:moduleC`
- `:lib:moduleD`

### 排除特定模块

```kotlin
plugins {
    id("site.addzero.modules-buddy") version "+"
}

autoModules {
    excludeModules.set(arrayOf("lib"))
}
```

只包含：
- `:moduleA`
- `:moduleB`

## 工作原理

1. 扫描项目根目录下的所有子目录
2. 查找包含 `build.gradle.kts` 的目录
3. 排除 `excludeModules` 中指定的模块
4. 自动调用 `include()` 包含找到的模块
