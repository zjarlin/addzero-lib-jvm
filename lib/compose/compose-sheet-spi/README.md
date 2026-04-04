# compose-sheet-spi

面向在线 Excel / 腾讯文档式表格的 renderless sheet engine。

- Maven 坐标：`site.addzero:compose-sheet-spi`
- 本地路径：`lib/compose/compose-sheet-spi`
- 适用场景：在线单元格编辑、工作表切换、虚拟滚动视口、操作日志、局部协同状态

## 设计目标

- `SheetState` 只管理交互态：当前工作表、视口、选择区、编辑草稿
- `SheetController` 只管理加载、操作提交、历史栈、并发保护
- `SheetReducer` 只负责把 `SheetOperation` 落到文档快照
- UI 层只消费 `document + state + controller`

## 最小示例

```kotlin
val controller = rememberSheetController(
    dataSource = sheetDataSource,
    documentId = "asset-config",
)

controller.selectRange(
    SheetRange.single(SheetCellAddress(rowIndex = 0, columnIndex = 0)),
)
controller.startEditing(SheetCellAddress(rowIndex = 0, columnIndex = 0))
controller.updateEditingInput("设备编号")
controller.commitEditing()

controller.pastePlainText("设备编号\t启用\nA-001\ttrue")
val exported = controller.selectedRangeAsPlainText()
```

## 边界

- 这个模块只提供 sheet engine 骨架，不内置公式求值器
- 多人协同的 OT / CRDT、presence 广播、权限与审计由上层数据源接入
- 如果只是服务端分页 CRUD 列表，请继续用 `compose-crud-spi`
- 纯文本复制/粘贴默认按 `TSV/换行` 处理，适合表格与剪贴板互通

## 独立启动验证

- 运行纯引擎场景：
  `./gradlew :lib:compose:compose-sheet-spi:runSheetEngineScenario`
