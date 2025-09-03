# TableOriginal 优化迁移指南

## 概述

本次优化主要解决了以下问题：
1. **状态管理混乱** - 引入 `TableViewModel` 统一管理表格状态
2. **子组件传参复杂** - 通过 ViewModel 减少参数传递层级
3. **rowActionSlot 缺少行信息** - 新版本支持传递 `item` 和 `index` 参数

## 主要改进

### 1. 引入 TableViewModel

```kotlin
// 新版本 - 使用 ViewModel
val tableViewModel = rememberTableViewModel<DataType, ColumnType>()

TableOriginal(
    // ... 其他参数
    viewModel = tableViewModel
)
```

**优势：**
- 统一状态管理
- 减少参数传递
- 更好的性能优化
- 支持复杂业务逻辑

### 2. 优化的插槽系统

#### 旧版本 rowActionSlot
```kotlin
// 旧版本 - 无法获取行数据
rowActionSlot = {
    AddEditDeleteButton(
        onEditClick = { /* 无法知道编辑哪一行 */ },
        onDeleteClick = { /* 无法知道删除哪一行 */ }
    )
}
```

#### 新版本 rowActionSlot
```kotlin
// 新版本 - 支持行数据和索引
rowActionSlot = { item, index ->
    AddEditDeleteButton(
        onEditClick = { 
            println("编辑第 ${index + 1} 行: ${item.name}")
        },
        onDeleteClick = { 
            println("删除 ID: ${item.id} 的记录")
        }
    )
}
```

#### 新增的插槽类型
```kotlin
TableSlots(
    // 行左侧插槽 - 支持复选框等
    rowLeftSlot = { item, index ->
        Checkbox(
            checked = selectedItems.contains(item.id),
            onCheckedChange = { /* 处理选择逻辑 */ }
        )
    },
    
    // 单元格级别操作插槽
    cellActionSlot = { item, column, value, rowIndex, columnIndex ->
        // 可以针对特定单元格添加操作
    },
    
    // 列级别插槽
    columnLeftSlot = { column, columnIndex -> /* 列左侧内容 */ },
    columnRightSlot = { column, columnIndex -> /* 列右侧内容 */ },
    
    // 新增状态插槽
    loadingSlot = { /* 自定义加载状态 */ },
    errorSlot = { error -> /* 自定义错误显示 */ }
)
```

## 迁移步骤

### 步骤 1: 更新导入
```kotlin
import com.addzero.component.table.original.TableViewModel
import com.addzero.component.table.original.rememberTableViewModel
```

### 步骤 2: 创建 ViewModel（可选）
```kotlin
// 如果需要复杂状态管理
val tableViewModel = rememberTableViewModel<YourDataType, YourColumnType>()
```

### 步骤 3: 更新 rowActionSlot
```kotlin
// 旧版本
rowActionSlot = {
    MyActionButtons()
}

// 新版本
rowActionSlot = { item, index ->
    MyActionButtons(
        onEdit = { editItem(item) },
        onDelete = { deleteItem(item.id) }
    )
}
```

### 步骤 4: 更新 rowLeftSlot（如果使用）
```kotlin
// 旧版本
rowLeftSlot = {
    MyLeftContent()
}

// 新版本
rowLeftSlot = { item, index ->
    MyLeftContent(item = item, index = index)
}
```

## 兼容性

### 向后兼容
- 旧版本的 API 仍然可用，使用 `TableOriginalLegacy`
- 现有代码无需立即迁移

### 兼容性辅助函数
```kotlin
// 如果需要使用旧版本的 rowActionSlot
val legacySlots = TableSlots<DataType>().withLegacyRowActionSlot {
    MyOldActionButtons()
}
```

## 最佳实践

### 1. 使用 ViewModel 进行复杂状态管理
```kotlin
class MyTableViewModel<T, C> : TableViewModel<T, C>() {
    var selectedItems by mutableStateOf(setOf<Any>())
    var searchKeyword by mutableStateOf("")
    
    fun toggleSelection(item: T) {
        val id = getRowId(item)
        selectedItems = if (selectedItems.contains(id)) {
            selectedItems - id
        } else {
            selectedItems + id
        }
    }
    
    fun getFilteredData(): List<T> {
        return data.filter { item ->
            // 实现搜索逻辑
        }
    }
}
```

### 2. 充分利用新的插槽系统
```kotlin
TableSlots(
    topSlot = {
        // 搜索栏、工具栏等
        MySearchAndToolbar()
    },
    rowLeftSlot = { item, index ->
        // 复选框、序号等
        MyRowSelector(item, index)
    },
    rowActionSlot = { item, index ->
        // 行操作按钮
        MyRowActions(item, index)
    },
    bottomSlot = {
        // 分页、统计信息等
        MyPaginationAndStats()
    }
)
```

### 3. 性能优化建议
```kotlin
// 使用 remember 缓存复杂计算
val processedData = remember(rawData, searchKeyword) {
    rawData.filter { /* 过滤逻辑 */ }
}

// 使用 derivedStateOf 进行响应式计算
val selectedCount by remember {
    derivedStateOf { selectedItems.size }
}
```

## 示例对比

### 旧版本完整示例
```kotlin
TableOriginal(
    columns = columns,
    data = data,
    getColumnKey = { it.key },
    getColumnLabel = { Text(it.label) },
    getRowId = { it.id },
    slots = TableSlots(
        rowActionSlot = {
            // 无法获取当前行数据
            Button(onClick = { /* ??? */ }) {
                Text("操作")
            }
        }
    )
)
```

### 新版本完整示例
```kotlin
TableOriginal(
    columns = columns,
    data = data,
    getColumnKey = { it.key },
    getColumnLabel = { Text(it.label) },
    getRowId = { it.id },
    slots = TableSlots(
        rowActionSlot = { item, index ->
            // 可以访问当前行数据
            Button(onClick = { handleAction(item) }) {
                Text("操作 ${item.name}")
            }
        },
        rowLeftSlot = { item, index ->
            Checkbox(
                checked = isSelected(item.id),
                onCheckedChange = { toggleSelection(item) }
            )
        }
    ),
    viewModel = tableViewModel // 可选的 ViewModel
)
```

## 注意事项

1. **类型安全**: 新版本提供更好的类型安全性
2. **性能**: ViewModel 版本在大数据量时性能更好
3. **扩展性**: 新的插槽系统支持更复杂的自定义需求
4. **测试**: 建议在测试环境中验证迁移后的功能

## 常见问题

**Q: 是否必须使用 ViewModel？**
A: 不是必须的。如果你的表格逻辑简单，可以继续使用原有方式。

**Q: 旧版本代码何时会被移除？**
A: 暂无移除计划，会保持向后兼容。

**Q: 如何处理复杂的行操作？**
A: 使用新的 rowActionSlot，可以访问完整的行数据进行复杂操作。
