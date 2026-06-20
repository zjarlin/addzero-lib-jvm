# tool-printer

本地打印工具。

Maven 坐标：

```kotlin
implementation("site.addzero:tool-printer")
```

主要能力：

- `PrinterUtil.printFile(filePath, copies)`：向默认打印机发送文件。
- `PrinterUtil.printFilesInDirectory(directoryPath, copies)`：批量打印目录内可打印文件。
