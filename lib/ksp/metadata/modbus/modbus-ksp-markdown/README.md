# modbus-ksp-markdown

`modbus-ksp-markdown` 是 Modbus 协议文档生成器模块，它通过 `ServiceLoader` 向 `modbus-ksp-core` 注册 `MARKDOWN_PROTOCOL` 生成器。

- Maven 坐标：`site.addzero:modbus-ksp-markdown`
- 本地路径：`lib/ksp/metadata/modbus/modbus-ksp-markdown`

## 它生成什么

这个模块负责生成每个 service 的协议说明文档，包括：

- `<service>.<transport>.protocol.md`
- `<service>_bridge_sample.c`

这里的 `*_bridge_sample.c` 是只读模板参考文件：

- 用来对照最新桥接函数签名和注释
- 不参与固件正式编译
- 当 `*_bridge_impl.c` 已经存在且不会被覆盖时，它尤其有用

## 谁应该直接依赖它

通常不是业务模块。

直接依赖它的应该是：

- `modbus-ksp-rtu`
- `modbus-ksp-tcp`
- `modbus-ksp-mqtt`
- 自定义 Modbus processor 组合模块

## 最小接法

```kotlin
dependencies {
    implementation(project(":lib:ksp:metadata:modbus:modbus-ksp-core"))
    implementation(project(":lib:ksp:metadata:modbus:modbus-ksp-markdown"))
}
```

## 业务层怎么“用”它

业务层通常通过更高层入口间接触发它，只要：

- `codegenModes` 包含 `contract`

就会在当前模块的 `build/generated/ksp/main/resources/generated/modbus/protocols` 下看到生成结果。

如果再配合：

- `addzero.modbus.c.output.projectDir`
- `addzero.modbus.markdown.output.path`

这些 `.md` 和 `*_bridge_sample.c` 还会被镜像到外部固件工程的文档目录，例如默认的 `Docs/generated/modbus/<transport>`。
