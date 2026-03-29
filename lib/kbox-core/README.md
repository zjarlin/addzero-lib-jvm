# kbox-core

`kbox-core` 提供 `KBox` 的本地文件扫描、安装包归档、远程路径规划和迁移历史记录能力。

- Maven 坐标：`site.addzero:kbox-core`
- 本地模块：`/Users/zjarlin/IdeaProjects/addzero-lib-jvm/lib/kbox-core`

最小用法：

```kotlin
val settings = settingsRepository.load()
val installers = installerService.scan(settings)
val archived = installerService.collect(installers)
val largeFiles = largeFileService.scan(settings)
```

运行约束：

- 当前模块仅支持 JVM。
- 本地数据目录通过 `site.addzero.util.PathUtil.appDataDir("KBox")` 解析。
