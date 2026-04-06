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
  - KMP 消费者会落到 `commonMainImplementation`
  - JVM 消费者会落到 `implementation`
- 把 typed DSL 转成底层 `ksp.arg(...)`

## 用法

```kotlin
plugins {
    id("site.addzero.ksp.modbus-rtu")
}

modbusRtu {
    transports.set(listOf("rtu"))
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
- `transports`
  - 会映射成 `addzero.modbus.transports`
  - 支持一次启用多个已实现 transport，例如 `listOf("rtu", "tcp")`
- `cOutputProjectDir`
  - 会映射成 `addzero.modbus.c.output.projectDir`
  - 配置后会把生成的 C 文件镜像到固件工程
- `bridgeImplPath`
  - 会映射成 `addzero.modbus.c.bridgeImpl.path`
  - 控制可编辑 bridge 实现目录，默认 `Core/Src/modbus`
- `keilUvprojxPath`
  - 会映射成 `addzero.modbus.keil.uvprojx.path`
  - 配置后会细粒度同步 `.uvprojx`
- `keilTargetName`
  - 会映射成 `addzero.modbus.keil.targetName`
- `keilGroupName`
  - 会映射成 `addzero.modbus.keil.groupName`
  - 默认 `Core/modbus/rtu`
- `mxprojectPath`
  - 会映射成 `addzero.modbus.mxproject.path`
  - 配置后会同步 `.mxproject`

固件工程联调用法：

```kotlin
modbusRtu {
    transports.set(listOf("rtu"))
    codegenModes.set(listOf("server", "contract"))
    contractPackages.set(listOf("site.addzero.device.contract"))

    cOutputProjectDir.set("/Users/zjarlin/IdeaProjects/t")
    bridgeImplPath.set("Core/Src/modbus")
    keilUvprojxPath.set("MDK-ARM/test1.uvprojx")
    keilTargetName.set("test1")
    keilGroupName.set("Core/modbus/rtu")
    mxprojectPath.set(".mxproject")
}
```

生成后的目录约定：

- 请勿手动修改：
  - `Core/Inc/generated/modbus/rtu/...`
  - `Core/Src/generated/modbus/rtu/...`
- 需要接业务逻辑：
  - `Core/Src/modbus/rtu/<service>/<service>_bridge_impl.c`

项目文件同步范围：

- 会改：
  - `.uvprojx`
  - `.mxproject`
- 不会改：
  - `.uvoptx`
  - `.ioc`

## 兼容说明

底层处理器仍然是 `modbus-ksp-rtu`。

也就是说：

- 老工程继续手写 `ksp(project(":lib:ksp:metadata:modbus:modbus-ksp-rtu"))` 还能工作
- 新工程默认应该改成 `site.addzero.ksp.modbus-rtu`

推荐理由：

- 消费侧不再暴露 processor artifact 细节
- companion runtime 依赖不需要手工补
- 后续扩展参数时可以继续保持 typed DSL
