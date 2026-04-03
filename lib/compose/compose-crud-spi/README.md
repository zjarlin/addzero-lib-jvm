# compose-crud-spi

渲染无关的 KMP 表格控制层。

- Maven 坐标：`site.addzero:compose-crud-spi`
- 本地路径：`lib/compose/compose-crud-spi`
- 适用场景：服务端分页表格、搜索/筛选/排序/批量删除控制器

## 设计目标

- `CrudTableState` 只管理查询状态与选择状态
- `CrudTableController` 只管理副作用与异步调度
- `AddTable` 这类 Compose 成品表格只负责渲染

## 最小示例

```kotlin
val controller = rememberCrudTableController(
    dataSource = userDataSource,
    rowIdOf = { it.id },
)

AddTable(
    controller = controller,
    columns = columns,
    getColumnKey = { it.key },
    getRowId = { it.id },
)
```

## 与在线 Excel 的边界

`compose-crud-spi` 只服务 CRUD 列表表格，不负责在线单元格编辑、公式计算、协同冲突解决、操作日志或 CRDT/OT。
腾讯文档那类在线表格应该单独做 grid/sheet engine，而不是继续堆在 `AddTable` 上。
