# modbus-rtu-gradle-plugin

项目级 Modbus RTU KSP 消费插件。

- Plugin id: `site.addzero.ksp.modbus-rtu`
- Maven 坐标：`site.addzero:modbus-rtu-gradle-plugin`
- 本地路径：`lib/ksp/metadata/modbus/modbus-rtu-gradle-plugin`

## 作用

这个模块是 `modbus-ksp-rtu` 的推荐消费入口。

它负责：

- 应用 `com.google.devtools.ksp`
- 注入 `site.addzero:modbus-ksp-rtu`
- 自动补 `site.addzero:modbus-runtime`
- 把 typed DSL 转成底层 `ksp.arg(...)`

## 用法

```kotlin
plugins {
    id("site.addzero.ksp.modbus-rtu")
}

modbusRtu {
    codegenModes.set(listOf("server"))
    contractPackages.set(listOf("site.addzero.device.contract"))
}
```

## 跨仓库本地联调

如果消费仓库不是 `addzero-lib-jvm` 自己，而是像 `kmp-aio` 这种会把 `../addzero-lib-jvm` 的部分模块 remap 成本地 project path 的工程，推荐保留正常的插件 DSL：

```kotlin
plugins {
    id("site.addzero.ksp.modbus-rtu")
}

modbusRtu {
    codegenModes.set(listOf("server"))
    contractPackages.set(listOf("site.addzero.device.contract"))
}
```

但要先把插件 artifact 发布到 `mavenLocal`：

```bash
cd /Users/zjarlin/IdeaProjects/addzero-lib-jvm
./gradlew \
  :lib:gradle-plugin:project-plugin:gradle-ksp-consumer-base:publishToMavenLocal \
  :lib:ksp:metadata:modbus:modbus-rtu-gradle-plugin:publishToMavenLocal
```

原因很直接：

- 这仍然是项目级 Gradle plugin 接法，不是回退到手写 `ksp(...)`
- 消费仓库继续用稳定的 `plugins {}` / typed DSL
- 本地联调时，处理器和 runtime 仍然可以优先绑定到 remap 进来的 project path
- 不需要把整个 `addzero-lib-jvm` 通过 `pluginManagement.includeBuild(...)` 塞进消费仓库

在当前 `kmp-aio <-> addzero-lib-jvm` 组合里，整仓 `pluginManagement.includeBuild(...)` 会把构建带到 `modbus-ksp-core` 的缺失 project path 问题，所以不推荐那样接。

当前 DSL：

- `codegenModes`
  - 会映射成 `addzero.modbus.codegen.mode`
- `contractPackages`
  - 会映射成 `addzero.modbus.contractPackages`

## 兼容说明

底层处理器仍然是 `modbus-ksp-rtu`。

也就是说：

- 老工程继续手写 `ksp(project(":lib:ksp:metadata:modbus:modbus-ksp-rtu"))` 还能工作
- 新工程默认应该改成 `site.addzero.ksp.modbus-rtu`

推荐理由：

- 消费侧不再暴露 processor artifact 细节
- companion runtime 依赖不需要手工补
- 后续扩展参数时可以继续保持 typed DSL
