# 表格组件索引

## 覆盖模块

- `compose-native-component-table-core`
- `compose-native-component-table`
- `compose-native-component-table-pro`
- `compose-model-component`

## 适合场景

- 典型 CRUD 列表页
- 带分页、排序、筛选、批量勾选、导入导出的管理台表格
- 需要自定义单元格渲染，但又不想自己拼表头、表体、分页栏

## 依赖入口

```kotlin
implementation(projects.lib.compose.composeNativeComponentTable)
implementation(projects.lib.compose.composeNativeComponentTablePro)
```

## 核心模型

- `ColumnConfig`：列宽、注释、类型等列配置
- `TableLayoutConfig`：布局参数
- `StatePagination`：分页状态
- `StateSearch`：字段筛选状态
- `StateSort`：排序状态

## 组件速查

### 原始表格层

- `TableOriginal<T, C>`：最底层通用表格
- `rememberAddTableAutoWidth`：自动宽度计算
- `RenderTableHeaderRow`
- `RenderTableBodyRow`
- `RenderTableScrollableContent`
- `RenderFixedIndexColumn`
- `RenderFixedActionColumn`

### 业务成品层

- `AddTable<T, C>`：自带搜索、排序、筛选、分页、批量操作的业务表格入口
- `AddTablePagination`
- `RenderButtons`
- `RenderPagination`
- `RenderCheckbox`
- `RenderSortButton`
- `RenderFilterButton`
- `RenderAdvSearchDrawer`
- `RenderSelectContent`

## 最小组合示例

```kotlin
@Composable
fun UserTable(users: List<User>, columns: List<UserColumn>) {
    AddTable(
        data = users,
        columns = columns,
        getColumnKey = { it.key },
        getRowId = { it.id },
        onSearch = { keyword, filters, sorts, pagination -> },
        onSaveClick = {},
        onImportClick = {},
        onExportClick = { keyword, filters, sorts, pagination -> },
        onBatchDelete = {},
        onBatchExport = {},
        onEditClick = {},
        onDeleteClick = {},
        getColumnLabel = { column -> Text(column.label) },
        getCellContent = { row, column -> Text(column.render(row)) }
    )
}
```

```kotlin
@Composable
fun SimpleTable(users: List<User>, columns: List<UserColumn>) {
    TableOriginal(
        data = users,
        columns = columns,
        getColumnKey = { it.key },
        getRowId = { it.id },
        getColumnLabel = { Text(it.label) },
        getCellContent = { row, column -> Text(column.render(row)) }
    )
}
```

## 选择建议

- 只需要表格骨架和插槽控制，用 `TableOriginal`
- 需要搜索、分页、排序、批量操作，直接用 `AddTable`
- 表格状态不要散落在页面里，优先围绕 `StatePagination` / `StateSearch` / `StateSort` 收口
