# TableOriginal 优化总结

## 优化目标
解决TableOriginal组件的以下问题：
1. **状态管理混乱** - 子组件传参复杂，状态分散
2. **参数传递冗余** - 每个子组件都需要传递大量参数
3. **rowActionSlot缺少行信息** - 无法获取当前行的数据和索引

## 优化方案

### 1. 引入TableViewModel统一状态管理

**之前的问题：**
```kotlin
// 参数分散，难以管理
TableOriginal(
    columns = columns,
    data = data,
    getColumnKey = { it.key },
    getColumnLabel = { Text(it.label) },
    getCellContent = { item, column -> /* ... */ },
    getRowId = { it.id },
    config = config,
    slots = slots
)
```

**优化后的方案：**
```kotlin
// 所有参数都封装在ViewModel中
val viewModel = rememberTableViewModel(
    columns = columns,
    data = data,
    getColumnKey = { it.key },
    getColumnLabel = { Text(it.label) },
    getCellContent = { item, column -> /* ... */ },
    getRowId = { it.id },
    config = config,
    slots = slots
)

TableOriginal(viewModel = viewModel)
```

### 2. 使用扩展函数简化子组件访问

**之前的问题：**
```kotlin
// 每个子组件都需要传递vm参数
@Composable
private fun <T, C> TableMainContent(
    vm: TableViewModel<T, C>,
    tableState: TableState<T, C>,
    modifier: Modifier = Modifier
)
```

**优化后的方案：**
```kotlin
// 使用扩展函数，直接访问ViewModel属性
@Composable
private fun <T, C> TableViewModel<T, C>.TableMainContent(
    tableState: TableState<T, C>,
    modifier: Modifier = Modifier
) {
    // 直接访问 data, columns, config, slots 等属性
}
```

### 3. 优化TableSlots支持行数据传递

**之前的问题：**
```kotlin
// 无法获取行数据
rowActionSlot = {
    Button(onClick = { /* 不知道操作哪一行 */ }) {
        Text("操作")
    }
}
```

**优化后的方案：**
```kotlin
// 支持行数据和索引传递
rowActionSlot = { item, index ->
    Button(onClick = { 
        println("操作第 ${index + 1} 行: ${item.name}")
    }) {
        Text("操作 ${item.name}")
    }
}
```

## 优化效果

### 1. API简化
- **之前**: 需要传递8-10个参数
- **现在**: 只需要传递1个ViewModel参数

### 2. 状态管理统一
- **之前**: 状态分散在各个组件中
- **现在**: 所有状态都在ViewModel中统一管理

### 3. 子组件参数传递简化
- **之前**: 每个子组件都需要传递多个参数
- **现在**: 使用扩展函数直接访问ViewModel属性

### 4. 插槽功能增强
- **之前**: rowActionSlot无法获取行信息
- **现在**: 支持传递item和index，可以进行精确操作

## 使用示例

### 基础用法
```kotlin
@Composable
fun MyTable() {
    val viewModel = rememberTableViewModel(
        columns = listOf("name", "age", "email"),
        data = userList,
        getColumnKey = { it },
        getColumnLabel = { Text(it) },
        getCellContent = { item, column -> 
            Text(when(column) {
                "name" -> item.name
                "age" -> item.age.toString()
                "email" -> item.email
                else -> ""
            })
        },
        getRowId = { it.id },
        slots = TableSlots(
            rowActionSlot = { item, index ->
                Row {
                    Button(onClick = { editUser(item) }) {
                        Text("编辑")
                    }
                    Button(onClick = { deleteUser(item.id) }) {
                        Text("删除")
                    }
                }
            }
        )
    )
    
    TableOriginal(viewModel = viewModel)
}
```

### 高级用法（动态数据更新）
```kotlin
@Composable
fun DynamicTable() {
    var searchKeyword by remember { mutableStateOf("") }
    
    val filteredData by remember {
        derivedStateOf {
            if (searchKeyword.isEmpty()) {
                originalData
            } else {
                originalData.filter { it.name.contains(searchKeyword, ignoreCase = true) }
            }
        }
    }
    
    val viewModel = rememberTableViewModel(
        columns = columns,
        data = filteredData, // 动态数据
        // ... 其他配置
    )
    
    Column {
        SearchBar(
            value = searchKeyword,
            onValueChange = { searchKeyword = it }
        )
        
        TableOriginal(viewModel = viewModel)
    }
}
```

## 向后兼容性

为了保持向后兼容，我们提供了`TableOriginalWithParams`函数：

```kotlin
// 旧代码仍然可以工作
TableOriginalWithParams(
    columns = columns,
    data = data,
    getColumnKey = { it.key },
    // ... 其他参数
)
```

## 性能优化

1. **状态缓存**: ViewModel中使用`derivedStateOf`进行性能缓存
2. **记忆化**: 使用`remember`避免重复计算
3. **懒加载**: 只渲染可见的表格行

## 总结

通过引入ViewModel模式和扩展函数，我们成功地：
- ✅ 简化了API，从多参数变为单参数
- ✅ 统一了状态管理，避免状态分散
- ✅ 解决了子组件传参混乱的问题
- ✅ 增强了插槽功能，支持行数据传递
- ✅ 保持了向后兼容性
- ✅ 提升了代码的可维护性和可读性

这次优化大大提升了TableOriginal组件的易用性和可维护性，为后续的功能扩展奠定了良好的基础。
