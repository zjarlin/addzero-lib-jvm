# KSP Buddy Plugin - Gradle 刷新问题修复

## 问题描述
原来的 `gradle-ksp-buddy` 插件在生成 KSP 代码后，IDE 需要手动刷新 Gradle 项目才能识别新生成的文件，导致编译错误显示为红色。

## 根本原因
1. 生成的代码文件没有被正确添加到 Kotlin 源集
2. Gradle 构建模型没有被重新评估
3. IDE 不知道新生成的文件应该参与编译

## 解决方案

### 1. 自动源集注册
```kotlin
// 将生成的代码目录添加到 Kotlin 源集
project.afterEvaluate {
    project.plugins.withType<org.jetbrains.kotlin.gradle.plugin.KotlinBasePlugin> {
        val kotlinExtension = project.extensions.getByName("kotlin") as
            org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension

        when (kotlinExtension) {
            is org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension -> {
                kotlinExtension.sourceSets.getByName("commonMain") {
                    kotlin.srcDir(generateTask.flatMap { it.generatedCodeOutputDir })
                }
            }
            is org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension -> {
                kotlinExtension.sourceSets.getByName("main") {
                    kotlin.srcDir(generateTask.flatMap { it.generatedCodeOutputDir })
                }
            }
        }
    }
}
```

### 2. 任务依赖管理
```kotlin
// 让生成任务在 compileKotlin 之前执行
project.tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    dependsOn(generateTask)
}
```

### 3. Gradle 刷新助手
创建了 `GradleRefreshHelper` 类来：
- 生成刷新标记文件
- 为 IDE 提供刷新提示
- 监控刷新状态

## 主要改动

### 1. KspBuddyPlugin.kt
- ✅ 添加了自动源集注册
- ✅ 修复了任务依赖关系
- ✅ 集成了 Gradle 刷新助手
- ✅ 改进了输出目录管理

### 2. 新增 GradleRefreshHelper.kt
- ✅ 处理 Gradle 重新评估
- ✅ 生成 IDE 刷新提示
- ✅ 监控刷新状态

### 3. 改进 GenerateKspScriptTask
- ✅ 添加了输出目录声明
- ✅ 返回生成的文件列表
- ✅ 更好的错误处理

## 使用方法

现在当你使用 `gradle-ksp-buddy` 插件时：

1. 生成的代码会自动添加到 Kotlin 源集
2. IDE 会自动识别新生成的文件
3. 不再需要手动刷新 Gradle 项目

## 配置示例

```kotlin
plugins {
    id("site.addzero.ksp-buddy")
}

kspBuddy {
    mustMap.set(mapOf(
        "config.key1" to "value1",
        "config.key2" to "value2"
    ))
    generatePrecompiledScript.set(true)

    settingContext {
        packageName.set("com.example.generated")
        enabled.set(true)
    }
}
```

## 故障排除

如果仍然遇到 IDE 刷新问题：

1. 检查 `build/ksp-buddy-ide-hints.txt` 文件
2. 手动执行 `./gradlew generateKspScript`
3. 在 IDE 中执行 "Reload Gradle Projects"

## 技术细节

### 自动源集注册流程
1. 插件检测 Kotlin 插件类型（KMP 或 JVM）
2. 自动将 `build/generated/ksp-buddy` 目录添加到对应源集
3. 确保 Gradle 构建包含生成的代码

### 任务依赖优化
1. `generateKspScript` 任务现在在 `compileKotlin` 之前执行
2. 使用 Gradle 的增量构建机制
3. 避免不必要的重新生成

### IDE 集成改进
1. 生成刷新标记文件触发 Gradle 重新评估
2. 提供详细的 IDE 刷新提示
3. 监控刷新状态和时机

这个解决方案应该能显著改善 KSP 代码生成后的 IDE 识别问题。