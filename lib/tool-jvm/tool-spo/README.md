# tool-spo

SPO 三元组数据结构与制表符文件读取工具。

Maven 坐标：

```kotlin
implementation("site.addzero:tool-spo")
```

主要能力：

- `Spo`：描述 `subject`、`predicate`、`object`、`context` 四段式结构。
- `SpoFileReader.readTab(filePath)`：读取带表头的 tab 分隔文本，返回 `List<Map<String, String>>`。
