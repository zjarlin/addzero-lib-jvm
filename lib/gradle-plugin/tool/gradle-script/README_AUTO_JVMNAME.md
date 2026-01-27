# Auto JvmName Gradle Plugin

自动为包含顶层函数/属性的 Kotlin 文件添加 `@file:JvmName` 注解。

## 功能特性

- ✅ 自动检测包含顶层函数或顶层属性的 Kotlin 文件
- ✅ 约定文件名（去除 .kt 后缀）作为 JvmName
- ✅ 跳过已有 `@file:JvmName` 注解的文件
- ✅ 跳过纯类定义文件（无顶层声明）
- ✅ 支持 package 声明后插入注解

## 使用方法

### 1. 在项目 build.gradle.kts 中应用插件

```kotlin
plugins {
    id("site.addzero.buildlogic.auto-jvmname")
}
```

### 2. 手动运行任务

```bash
# 为所有 Kotlin 文件添加 @file:JvmName 注解
./gradlew autoJvmName

# 为特定模块运行
./gradlew :lib:tool-jvm:tool-spctx:autoJvmName
```

### 3. 自动运行（可选）

在 `auto-jvmname.gradle.kts` 中取消注释以下行：

```kotlin
tasks.named("compileKotlin") {
    dependsOn("autoJvmName")  // 取消注释以在编译前自动运行
}
```

这样每次编译前会自动检查并添加注解。

## 示例

### 处理前

```kotlin
package site.addzero.util.spring

import org.springframework.context.ApplicationContext

fun getBean(clazz: Class<*>): Any? {
    // ...
}
```

### 处理后

```kotlin
package site.addzero.util.spring

@file:JvmName("SprCtxUtil")

import org.springframework.context.ApplicationContext

fun getBean(clazz: Class<*>): Any? {
    // ...
}
```

## 工作原理

1. 扫描 `src/main/kotlin` 目录下所有 `.kt` 文件
2. 检查文件是否包含顶层声明（函数、属性）
3. 跳过已有 `@file:JvmName` 的文件
4. 使用文件名作为 JvmName 值
5. 在 package 声明后插入注解

## 排除规则

自动跳过：
- `build/` 目录下的文件
- `test/` 目录下的测试文件
- `androidTest/` 目录下的测试文件
- 已有 `@file:JvmName` 注解的文件
- 不包含顶层声明的文件（纯类定义）

## 注意事项

⚠️ **首次使用建议**：
1. 先在小模块上测试
2. 使用版本控制检查变更
3. 确认生成的 JvmName 符合预期

⚠️ **命名约定**：
- 文件名应该符合 Java 类命名规范（首字母大写）
- 例如：`SprCtxUtil.kt` → `@file:JvmName("SprCtxUtil")`
- 避免使用特殊字符或空格

## 批量处理

为所有子模块添加注解：

```bash
# 根目录运行
./gradlew autoJvmName

# 或者针对特定目录
./gradlew :lib:tool-jvm:autoJvmName
```

## 自定义配置

如需自定义行为，可以修改 `auto-jvmname.gradle.kts` 文件：

```kotlin
abstract class AutoJvmNameTask : DefaultTask() {
    
    // 自定义源码目录
    @get:InputFiles
    val sourceFiles: FileTree = project.fileTree("src/main/kotlin") {
        include("**/*.kt")
        exclude("**/internal/**")  // 排除 internal 包
    }
    
    // ... 其他配置
}
```

## FAQ

**Q: 会修改已有的 @file:JvmName 注解吗？**  
A: 不会，插件会跳过已有注解的文件。

**Q: 如何为不同文件指定不同的 JvmName？**  
A: 手动添加 `@file:JvmName("自定义名称")`，插件会自动跳过。

**Q: 支持 Kotlin Multiplatform 吗？**  
A: 当前仅支持 JVM 平台，可以修改插件适配 KMP。

**Q: 性能影响如何？**  
A: 仅在手动执行任务时处理，不影响常规编译速度。如启用自动运行，首次处理后因文件已有注解会快速跳过。
