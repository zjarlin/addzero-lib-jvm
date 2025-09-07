# Compose Table Component

Compose Table Component 是一个功能丰富的表格组件，专为 Jetpack Compose 设计，支持固定列、自适应宽度、自定义渲染等多种特性。

## 功能特性

- **响应式数据绑定**：支持动态数据更新和响应式渲染
- **固定列支持**：支持固定序号列和操作列
- **自适应列宽**：根据内容自动计算最优列宽
- **自定义渲染**：支持自定义表头、单元格、行等渲染逻辑
- **灵活配置**：提供丰富的配置选项
- **Compose Props 集成**：与 Compose Props 处理器无缝集成

## 使用方法

### 1. 基本用法

```kotlin
@Composable
fun BasicTableExample() {
    val data = listOf(
        Person("张三", 25, "工程师"),
        Person("李四", 30, "设计师")
    )
    
    val columns = listOf("姓名", "年龄", "职位")
    
    TableOriginal(
        data = data,
        columns = columns,
        getColumnKey = { it },
        getRowId = { it.name }
    ) { person, column ->
        when (column) {
            "姓名" -> Text(person.name)
            "年龄" -> Text(person.age.toString())
            "职位" -> Text(person.job)
        }
    }
}
```

### 2. 高级配置

```kotlin
@Composable
fun AdvancedTableExample() {
    val data = getPersonData()
    val columns = getColumns()
    
    val columnConfigs = listOf(
        ColumnConfig(
            key = "name",
            label = "姓名",
            width = 120.dp,
            fixed = true
        ),
        ColumnConfig(
            key = "age",
            label = "年龄",
            textAlign = TextAlign.Center
        )
    )
    
    val layoutConfig = TableLayoutConfig(
        fixedIndexColumnWidth = 60.dp,
        fixedActionColumnWidth = 100.dp,
        showHeader = true,
        showFooter = true
    )
    
    TableOriginal(
        data = data,
        columns = columns,
        getColumnKey = { it.key },
        getRowId = { it.id },
        columnConfigs = columnConfigs,
        layoutConfig = layoutConfig,
        getColumnLabel = { config ->
            Text(
                text = config.label,
                fontWeight = FontWeight.Bold
            )
        }
    ) { person, column ->
        CellContent(person, column)
    }
}
```

## 核心组件

### TableOriginal

主要的表格组件，支持完整的表格功能。

#### 参数说明

- `data: List<T>` - 表格数据
- `columns: List<C>` - 列定义
- `getColumnKey: (C) -> String` - 获取列的唯一标识
- `getRowId: (T) -> Any` - 获取行的唯一标识
- `columnConfigs: List<ColumnConfig>` - 列配置
- `layoutConfig: TableLayoutConfig` - 布局配置
- `getColumnLabel: @Composable (C) -> Unit` - 自定义列标题渲染
- `topSlot: @Composable () -> Unit` - 顶部插槽
- `bottomSlot: @Composable () -> Unit` - 底部插槽
- `emptyContentSlot: @Composable () -> Unit` - 空内容插槽
- `getCellContent: @Composable (T, C) -> Unit` - 自定义单元格内容渲染
- `rowLeftSlot: @Composable (T, Int) -> Unit` - 行左侧插槽
- `rowActionSlot: (@Composable (T) -> Unit)?` - 行操作插槽
- `modifier: Modifier` - 修饰符

### ColumnConfig

列配置数据类，用于定义每列的显示特性。

#### 属性说明

- `key: String` - 列的唯一标识
- `label: String` - 列标题
- `width: Dp?` - 列宽度
- `minWidth: Dp` - 最小宽度
- `maxWidth: Dp` - 最大宽度
- `fixed: Boolean` - 是否固定列
- `textAlign: TextAlign` - 文本对齐方式
- `fontWeight: FontWeight?` - 字体粗细

### TableLayoutConfig

表格布局配置，用于定义表格的整体布局特性。

#### 属性说明

- `fixedIndexColumnWidth: Dp` - 固定序号列宽度
- `fixedActionColumnWidth: Dp` - 固定操作列宽度
- `showHeader: Boolean` - 是否显示表头
- `showFooter: Boolean` - 是否显示表尾
- `headerHeight: Dp` - 表头高度
- `rowHeight: Dp` - 行高度

## Compose Props 集成

TableOriginal 组件与 Compose Props 处理器集成，自动生成以下辅助工具：

### TableOriginalState

响应式状态管理类，用于管理表格的所有属性。

### rememberTableOriginalState

便捷函数，用于创建和记住表格状态。

### TableOriginalWidget

Widget 函数，接受状态类作为参数。

#### 使用示例

```kotlin
@Composable
fun TableWithPropsExample() {
    // 创建状态
    val state = rememberTableOriginalState(
        data = getPersonData(),
        columns = getColumns(),
        getColumnKey = { it.key },
        getRowId = { it.id }
    )
    
    // 使用 Widget 函数
    TableOriginalWidget(state = state)
    
    // 动态更新数据
    Button(onClick = { 
        state.data = getUpdatedPersonData()
    }) {
        Text("更新数据")
    }
}
```

## 自定义渲染

### 自定义列标题

```kotlin
TableOriginal(
    // ... 其他参数
    getColumnLabel = { columnConfig ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.LightGray),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = columnConfig.label,
                fontWeight = FontWeight.Bold
            )
        }
    }
)
```

### 自定义单元格内容

```kotlin
TableOriginal(
    // ... 其他参数
    getCellContent = { person, column ->
        when (column.key) {
            "name" -> {
                Text(
                    text = person.name,
                    color = if (person.age > 30) Color.Red else Color.Black
                )
            }
            "age" -> {
                Text(
                    text = person.age.toString(),
                    textAlign = TextAlign.Center
                )
            }
            else -> Text(person.toString())
        }
    }
)
```

### 自定义行操作

```kotlin
TableOriginal(
    // ... 其他参数
    rowActionSlot = { person ->
        Row {
            IconButton(onClick = { editPerson(person) }) {
                Icon(Icons.Default.Edit, contentDescription = "编辑")
            }
            IconButton(onClick = { deletePerson(person) }) {
                Icon(Icons.Default.Delete, contentDescription = "删除")
            }
        }
    }
)
```

## 性能优化

### 1. 固定列优化

通过固定序号列和操作列，提供更好的用户体验，特别是在水平滚动时。

### 2. 智能列宽计算

自动根据内容计算最优列宽，避免内容被截断或浪费空间。

### 3. 懒加载渲染

使用 LazyColumn 实现行的懒加载，提高大表格的渲染性能。

### 4. Compose Props 优化

通过 Compose Props 处理器，智能识别不需要响应式更新的参数，减少不必要的状态包装。

## 扩展功能

### 1. 顶部和底部插槽

```kotlin
TableOriginal(
    // ... 其他参数
    topSlot = {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Text(
                text = "表格顶部区域",
                modifier = Modifier.padding(16.dp)
            )
        }
    },
    bottomSlot = {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Text(
                text = "表格底部区域",
                modifier = Modifier.padding(16.dp)
            )
        }
    }
)
```

### 2. 空内容插槽

```kotlin
TableOriginal(
    // ... 其他参数
    emptyContentSlot = {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.Inbox,
                    contentDescription = "无数据",
                    modifier = Modifier.size(48.dp)
                )
                Text("暂无数据")
            }
        }
    }
)
```

## 最佳实践

### 1. 数据管理

```kotlin
@Composable
fun BestPracticeExample() {
    var persons by remember { mutableStateOf<List<Person>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        isLoading = true
        persons = loadPersons()
        isLoading = false
    }
    
    if (isLoading) {
        CircularProgressIndicator()
    } else {
        TableOriginal(
            data = persons,
            columns = getColumns(),
            getColumnKey = { it.key },
            getRowId = { it.id }
        ) { person, column ->
            // 渲染逻辑
        }
    }
}
```

### 2. 错误处理

```kotlin
@Composable
fun ErrorHandlingExample() {
    var persons by remember { mutableStateOf<List<Person>>(emptyList()) }
    var error by remember { mutableStateOf<String?>(null) }
    
    LaunchedEffect(Unit) {
        try {
            persons = loadPersons()
        } catch (e: Exception) {
            error = e.message
        }
    }
    
    error?.let { errorMessage ->
        Text(
            text = "加载失败: $errorMessage",
            color = Color.Red
        )
    } ?: run {
        TableOriginal(
            data = persons,
            // ... 其他参数
        )
    }
}
```

### 3. 使用 Compose Props 状态管理

```kotlin
@Composable
fun PropsManagedTableExample() {
    // 使用 Compose Props 处理器生成的状态管理
    val tableState = rememberTableOriginalState(
        data = emptyList(),
        columns = emptyList(),
        getColumnKey = { it.toString() },
        getRowId = { it.hashCode() },
        columnConfigs = emptyList(),
        layoutConfig = TableLayoutConfig(),
        getColumnLabel = { Text(it.toString()) },
        topSlot = {},
        bottomSlot = {},
        emptyContentSlot = {},
        getCellContent = { _, _ -> },
        rowLeftSlot = { _, _ -> },
        rowActionSlot = null,
        modifier = Modifier
    )
    
    // 加载数据
    LaunchedEffect(Unit) {
        tableState.data = loadPersons()
        tableState.columns = getColumns()
        // 更新其他属性...
    }
    
    // 使用 Widget 函数渲染
    TableOriginalWidget(state = tableState)
}
```

## 故障排除

### 常见问题

1. **表格不显示数据**：检查数据源是否正确传递
2. **列宽异常**：检查 ColumnConfig 配置是否正确
3. **性能问题**：对于大表格，考虑使用分页或虚拟化渲染
4. **状态更新不生效**：确保使用 rememberTableOriginalState 创建状态

### 调试技巧

1. 使用 Compose 预览功能验证组件渲染
2. 添加日志输出检查数据流
3. 使用 Layout Inspector 分析布局结构

## 许可证

[Apache License 2.0](LICENSE)