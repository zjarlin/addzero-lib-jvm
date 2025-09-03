# TableConfig 可序列化配置总结

## 🎯 优化目标达成

根据您的要求，我们成功将TableConfig改为可序列化的List<ColumnConfig>结构，方便后台配置：

### ✅ 1. 创建了可序列化的ColumnConfig
```kotlin
@Serializable
data class ColumnConfig(
    val key: String,           // 列的唯一标识符
    val label: String,         // 列显示名称
    val width: Float = 150f,   // 列宽度（dp）
    val visible: Boolean = true, // 是否可见
    val alignment: ColumnAlignment = ColumnAlignment.START, // 列对齐方式
    val dataType: ColumnDataType = ColumnDataType.TEXT,    // 数据类型
    val order: Int = 0         // 列顺序
)
```

### ✅ 2. 简化了TableConfig，只保留实际使用的配置
```kotlin
@Serializable
data class TableConfig(
    val columns: List<ColumnConfig> = emptyList(), // 列配置
    val headerCardType: String = "Dark",           // 表头配置
    val headerCornerRadius: Float = 8f,
    val headerElevation: Float = 4f,
    val rowHeight: Float = 56f,                    // 行配置
    val customProperties: String? = null           // 自定义配置
)
```

### ✅ 3. 移除了无关配置
按照您的要求，删除了组件未引用的所有配置项：
- ❌ 删除了排序、筛选、分页等未实现的功能配置
- ❌ 删除了边框、滚动等未使用的配置
- ❌ 删除了选择、导出等无关配置
- ✅ 只保留了实际使用的核心配置

## 🏗️ 配置结构

### 核心配置项
1. **列配置** - `List<ColumnConfig>`
   - 支持多列定义
   - 每列独立配置宽度、对齐、类型等
   
2. **表头配置** - 卡片类型、圆角、阴影
3. **行配置** - 行高度
4. **扩展配置** - 自定义属性JSON字符串

### 数据类型支持
```kotlin
@Serializable
enum class ColumnDataType {
    TEXT,        // 文本
    NUMBER,      // 数字
    DATE,        // 日期
    DATETIME,    // 日期时间
    BOOLEAN,     // 布尔值
    EMAIL,       // 邮箱
    URL,         // 链接
    PHONE,       // 电话
    CURRENCY,    // 货币
    PERCENTAGE,  // 百分比
    IMAGE,       // 图片
    CUSTOM       // 自定义
}
```

### 对齐方式支持
```kotlin
@Serializable
enum class ColumnAlignment {
    START,    // 左对齐
    CENTER,   // 居中对齐
    END       // 右对齐
}
```

## 🚀 使用方式

### 1. 程序化配置
```kotlin
val config = TableConfig(
    columns = listOf(
        ColumnConfig("name", "姓名", width = 120f, alignment = ColumnAlignment.START),
        ColumnConfig("age", "年龄", width = 80f, alignment = ColumnAlignment.CENTER, dataType = ColumnDataType.NUMBER),
        ColumnConfig("email", "邮箱", width = 200f, dataType = ColumnDataType.EMAIL)
    ),
    headerCardType = "Dark",
    headerCornerRadius = 8f,
    headerElevation = 4f,
    rowHeight = 56f
)
```

### 2. DSL配置
```kotlin
val config = buildTableConfig {
    columns {
        column("name", "姓名", width = 120f, alignment = ColumnAlignment.START, dataType = ColumnDataType.TEXT, order = 1)
        column("age", "年龄", width = 80f, alignment = ColumnAlignment.CENTER, dataType = ColumnDataType.NUMBER, order = 2)
        column("email", "邮箱", width = 200f, alignment = ColumnAlignment.START, dataType = ColumnDataType.EMAIL, order = 3)
    }
    header("Dark", 8f, 4f)
    rows(56f)
}
```

### 3. JSON配置（后台配置）
```json
{
    "columns": [
        {
            "key": "name",
            "label": "姓名",
            "width": 120.0,
            "visible": true,
            "alignment": "START",
            "dataType": "TEXT",
            "order": 1
        },
        {
            "key": "age",
            "label": "年龄", 
            "width": 80.0,
            "visible": true,
            "alignment": "CENTER",
            "dataType": "NUMBER",
            "order": 2
        }
    ],
    "headerCardType": "Dark",
    "headerCornerRadius": 8.0,
    "headerElevation": 4.0,
    "rowHeight": 56.0
}
```

### 4. 从JSON解析
```kotlin
val config = parseTableConfigFromJson(jsonString)
val viewModel = rememberTableViewModel(
    columns = config.visibleColumns,
    data = data,
    getColumnKey = { it.key },
    getColumnLabel = { Text(it.label) },
    // ...
    config = config
)
```

## 📊 序列化支持

### JSON序列化
```kotlin
// 序列化为JSON
val jsonString = config.toJson()

// 从JSON反序列化
val config = parseTableConfigFromJson(jsonString)
```

### 列配置序列化
```kotlin
// 序列化列配置
val columnsJson = columns.toJson()

// 从JSON解析列配置
val columns = parseColumnsFromJson(columnsJson)
```

## 🎯 后台配置优势

1. **完全可序列化** - 支持JSON格式，方便后台存储和传输
2. **类型安全** - 使用枚举确保配置值的有效性
3. **结构清晰** - 配置层次分明，易于理解和维护
4. **扩展性强** - 支持自定义属性，可根据需要扩展
5. **向后兼容** - 所有配置都有默认值，确保兼容性

## 📝 配置示例

### 完整的后台配置示例
```json
{
    "columns": [
        {
            "key": "id",
            "label": "ID",
            "width": 80.0,
            "visible": false,
            "alignment": "CENTER",
            "dataType": "NUMBER",
            "order": 0
        },
        {
            "key": "name",
            "label": "员工姓名",
            "width": 140.0,
            "visible": true,
            "alignment": "START", 
            "dataType": "TEXT",
            "order": 1
        },
        {
            "key": "age",
            "label": "年龄",
            "width": 80.0,
            "visible": true,
            "alignment": "CENTER",
            "dataType": "NUMBER", 
            "order": 2
        },
        {
            "key": "email",
            "label": "邮箱地址",
            "width": 220.0,
            "visible": true,
            "alignment": "START",
            "dataType": "EMAIL",
            "order": 3
        },
        {
            "key": "department",
            "label": "部门",
            "width": 120.0,
            "visible": true,
            "alignment": "START",
            "dataType": "TEXT",
            "order": 4
        },
        {
            "key": "salary",
            "label": "薪资",
            "width": 100.0,
            "visible": false,
            "alignment": "END",
            "dataType": "CURRENCY",
            "order": 5
        }
    ],
    "headerCardType": "Dark",
    "headerCornerRadius": 12.0,
    "headerElevation": 4.0,
    "rowHeight": 60.0,
    "customProperties": "{\"theme\":\"corporate\",\"locale\":\"zh-CN\"}"
}
```

## 🎉 总结

通过这次优化，我们成功地：

1. ✅ **实现了完全可序列化的配置系统** - 支持JSON格式
2. ✅ **简化了配置结构** - 只保留实际使用的配置项
3. ✅ **提供了多种配置方式** - 程序化、DSL、JSON三种方式
4. ✅ **确保了类型安全** - 使用枚举和强类型
5. ✅ **支持后台配置** - 完全可以通过API配置表格
6. ✅ **保持了向后兼容** - 所有配置都有合理的默认值

现在的TableConfig系统非常适合后台配置，可以轻松地通过API接口获取配置并应用到表格组件中！
