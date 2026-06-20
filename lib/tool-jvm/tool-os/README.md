# tool-os

操作系统与平台路径工具。

Maven 坐标：

```kotlin
implementation("site.addzero:tool-os")
```

主要能力：

- `OsUtil.getAppDataDir(appName)`：获取当前系统的应用数据目录。
- `OsUtil.getPlatformType()`：识别当前平台类型。
- `OsUtil.openFolder(path)`：按系统打开本地目录。
