# Gradle Modules Buddy (Gradle模块自动扫描插件)

自动模块插件，用于自动扫描项目目录并包含所有Gradle子项目。

## 功能

- 自动扫描项目根目录下的所有子目录
- 识别包含 `build.gradle.kts` 文件的目录作为Gradle模块
- 自动将这些模块包含到项目中，无需手动在 `settings.gradle.kts` 中添加 `include`
- 排除常见的非模块目录（如 `build`, `gradle`, `.git` 等）
- 支持可配置的黑名单目录

## 使用方法

在项目的 `settings.gradle.kts` 文件中应用插件：

```kotlin
    plugins {
    id("site.addzero.modules-buddy") version "+" // 版本号根据实际情况调整
}

```

## 配置选项

插件支持以下配置选项：

```kotlin
autoModules {
    // 自定义黑名单目录列表（这些目录及其子目录将被排除）
    blacklistedDirs = listOf("buildSrc", "build-logic", "lib", "custom-excluded-dir")
    
    // 自定义排除目录列表（用于过滤常见的非项目目录）
    excludedDirs = setOf(
        "build", "gradle", ".gradle", ".git", ".idea",
        "node_modules", "target", "out", "bin", ".settings",
        "src", "test", "main", "kotlin", "java", "resources",
        "custom-excluded-dir"
    )
}
```

## 默认配置

插件默认会排除以下目录：

### 黑名单目录（blacklistedDirs）：
- `buildSrc`
- `build-logic`
- `lib`

### 排除目录（excludedDirs）：
- `build`
- `gradle`
- `.gradle`
- `.git`
- `.idea`
- `node_modules`
- `target`
- `out`
- `bin`
- `.settings`
- `src`
- `test`
- `main`
- `kotlin`
- `java`
- `resources`

## 依赖配置

插件使用了 addzero-gradle-tool 库，可以通过以下方式在 build.gradle.kts 中配置依赖：

```kotlin
dependencies {
    implementation(libs.addzero.gradle.tool)
}
```

该依赖已经在项目的 `gradle/libs.versions.toml` 文件中正确定义。

## 工作原理

1. 插件会在应用时扫描项目根目录下的所有子目录
2. 查找包含 `build.gradle.kts` 文件的目录
3. 将这些目录作为模块自动包含到项目中
4. 设置每个模块的项目目录路径

## 示例

假设项目结构如下：
```
project/
├── settings.gradle.kts
├── build.gradle.kts
├── moduleA/
│   └── build.gradle.kts
├── moduleB/
│   └── build.gradle.kts
└── subproject/
    ├── moduleC/
    │   └── build.gradle.kts
    └── moduleD/
        └── build.gradle.kts
```

应用插件后，会自动包含以下模块：
- `:moduleA`
- `:moduleB`
- `:subproject:moduleC`
- `:subproject:moduleD`

无需手动在 `settings.gradle.kts` 中添加 `include` 语句。

### 自定义配置示例

```kotlin
plugins {
    id("site.addzero.auto-modules")
}

autoModules {
    // 只排除buildSrc和自定义目录，不排除build-logic和lib
    blacklistedDirs = listOf("buildSrc", "my-custom-excluded-dir")
    
    // 添加额外的排除目录
    excludedDirs = setOf(
        "build", "gradle", ".gradle", ".git", ".idea",
        "node_modules", "target", "out", "bin", ".settings",
        "src", "test", "main", "kotlin", "java", "resources",
        "my-custom-excluded-dir", "temp"
    )
}
```
