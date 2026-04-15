# modbus-ksp-keil-sync

`modbus-ksp-keil-sync` 是“生成后同步固件工程文件”的 SPI 模块。

- Maven 坐标：`site.addzero:modbus-ksp-keil-sync`
- 本地路径：`lib/ksp/metadata/modbus/modbus-ksp-keil-sync`

## 它解决什么问题

当处理器把 C 文件镜像到外部固件工程后，往往还要顺手把工程文件也同步一下，不然 IDE 或构建系统看不到新文件。

这个模块当前内置两个同步器：

- `KeilUvprojxSyncTool`
  - 细粒度更新 `.uvprojx`
- `MxprojectSyncTool`
  - 更新 STM32CubeMX `.mxproject` 的路径缓存

## 业务层怎么用到它

通常不是直接依赖，而是通过 RTU/TCP/MQTT 处理器间接启用。

只要你在更高层入口里配置了外部工程参数，这个模块就会参与工作：

```kotlin
modbusRtu {
    cOutputProjectDir.set("/absolute/path/to/firmware-project")
    bridgeImplPath.set("Core/Src/modbus")
    keilUvprojxPath.set("MDK-ARM/test1.uvprojx")
    keilTargetName.set("test1")
    keilGroupName.set("Core/modbus/rtu")
    mxprojectPath.set(".mxproject")
}
```

对应底层参数是：

- `addzero.modbus.c.output.projectDir`
- `addzero.modbus.keil.uvprojx.path`
- `addzero.modbus.keil.targetName`
- `addzero.modbus.keil.groupName`
- `addzero.modbus.mxproject.path`

## 谁应该直接依赖它

只有两类：

- 组装自定义 Modbus processor 的模块
- 专门测试工程文件同步行为的模块

普通业务工程不要直接依赖它。

## 它不会做什么

- 不会替你生成 C 文件
- 不会扫描 Kotlin 契约
- 不会管理 `.ioc`
- 不会碰 `.uvoptx`

它只负责在“外部源码已经生成出来之后”，把 `.uvprojx` / `.mxproject` 同步到能识别这些文件的状态。
