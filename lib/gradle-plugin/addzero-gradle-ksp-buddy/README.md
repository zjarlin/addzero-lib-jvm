# KSP Buddy Plugin

KSP Buddy是一个Gradle插件，用于简化KSP（Kotlin Symbol Processing）处理器的配置和使用。

## 功能特性

1. **自动生成KSP配置脚本**：根据配置的mustMap自动生成KSP配置脚本
2. **可配置的生成路径**：可以自定义KSP预编译脚本的生成路径(也就是用户所需要传递的ksp参数,自动生成在build-logic(buildSrc) 目录下)
3. **SettingContext生成**：根据mustMap内容自动生成Settings数据类和SettingContext对象
4. **生成文件位置**：生成的SettingContext和Settings类位于build目录下，避免污染源码
5. **无序列化依赖**：生成的代码不依赖kotlinx.serialization库
6. **模板化生成**：使用字符串模板方式生成代码，提高可读性和可维护性

## 使用方法

### 1. 应用插件

在你的模块的`build.gradle.kts`文件中应用插件：

```kotlin
plugins {
    id("ksp-buddy")
}
```

### 2. 配置插件

```kotlin
kspBuddy {
    // 配置 KSP 参数
    mustMap.set(
        mapOf(
            "enumOutputPackage" to "site.addzero.generated.enums",
            "dictTableName" to "sys_dict",
            "dictIdColumn" to "id",
            "dictCodeColumn" to "dict_code",
            "dictNameColumn" to "dict_name",
            "dictItemTableName" to "sys_dict_item",
            "dictItemForeignKeyColumn" to "dict_id",
            "dictItemCodeColumn" to "item_value",
            "dictItemNameColumn" to "item_text",
        )
    )

    // 配置 KSP 脚本输出路径（默认值,不配置就是这个）
    kspScriptOutputDir = "build-logic/src/main/kotlin/convention-plugins/generated"

    // 配置 SettingContext 生成
    settingContext = SettingContextConfig(
//                （默认值,不配置就是这个）

        contextClassName = "SettingContext",
//                （默认值,不配置就是这个）
        settingsClassName = "Settings",
//                （默认值,不配置就是这个）
        packageName = "site.addzero.context",
//                （默认jvm项目src/main/kotlin,这里举个kmp的例子）
        outputDir = "src/commonMain/kotlin",
        enabled = true
    )

}
```

### 3. 添加生成源码到编译路径（可选）

为了让编译器能够找到生成的源码，需要手动将生成目录添加到源码路径中：

```kotlin
kotlin {
    sourceSets {
        commonMain {
            kotlin.srcDir("build/generated/ksp-buddy")
        }
    }
}
```

或者对于JVM项目：

```kotlin
kotlin {
    sourceSets {
        main {
            kotlin.srcDir("build/generated/ksp-buddy")
        }
    }
}
```

### 4. 生成的文件

应用插件并配置后，插件会自动生成以下文件：

1. **KSP配置脚本**：在指定的输出目录中生成KSP配置脚本，文件名格式为`ksp-config4{moduleName}.gradle.kts`
2. **Settings数据类**：在`build/generated/ksp-buddy`目录中生成Settings数据类，类的属性完全基于mustMap的内容，不包含序列化注解
3. **SettingContext对象**：在`build/generated/ksp-buddy`目录中生成SettingContext对象，包含基于mustMap的初始化逻辑

## 示例

查看[示例模块](../demo/ksp-buddy-demo/build.gradle.kts)了解如何使用该插件。
