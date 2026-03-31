# modbus-tcp-gradle-plugin

项目级 Modbus TCP KSP 消费插件。

- Plugin id: `site.addzero.ksp.modbus-tcp`
- Maven 坐标：`site.addzero:modbus-tcp-gradle-plugin`
- 本地路径：`lib/ksp/metadata/modbus/modbus-tcp-gradle-plugin`

## 作用

这个模块是 `modbus-ksp-tcp` 的推荐消费入口。

它负责：

- 应用 `com.google.devtools.ksp`
- 注入 `site.addzero:modbus-ksp-tcp`
- 自动补 `site.addzero:modbus-runtime`
- 把 typed DSL 转成底层 `ksp.arg(...)`

## 用法

```kotlin
plugins {
    id("site.addzero.ksp.modbus-tcp")
}

modbusTcp {
    codegenModes.set(listOf("server"))
    contractPackages.set(listOf("site.addzero.device.contract"))
}
```

## 跨仓库本地联调

如果消费仓库通过 settings remap 方式引入 `../addzero-lib-jvm` 的 project path，推荐保留正常的插件 DSL：

```kotlin
plugins {
    id("site.addzero.ksp.modbus-tcp")
}

modbusTcp {
    codegenModes.set(listOf("server"))
    contractPackages.set(listOf("site.addzero.device.contract"))
}
```

但要先把插件 artifact 发布到 `mavenLocal`：

```bash
cd /Users/zjarlin/IdeaProjects/addzero-lib-jvm
./gradlew \
  :lib:gradle-plugin:project-plugin:gradle-ksp-consumer-base:publishToMavenLocal \
  :lib:ksp:metadata:modbus:modbus-tcp-gradle-plugin:publishToMavenLocal
```

这样仍然保持项目级 Gradle plugin 入口，同时避免把整仓 `addzero-lib-jvm` 塞进 `pluginManagement.includeBuild(...)`。

当前 DSL：

- `codegenModes`
  - 会映射成 `addzero.modbus.codegen.mode`
- `contractPackages`
  - 会映射成 `addzero.modbus.contractPackages`

## 兼容说明

底层处理器仍然是 `modbus-ksp-tcp`。

也就是说：

- 老工程继续手写 `ksp(project(":lib:ksp:metadata:modbus:modbus-ksp-tcp"))` 还能工作
- 新工程默认应该改成 `site.addzero.ksp.modbus-tcp`
