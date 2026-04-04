# compose-native-component-autocomplet

自动补全输入组件模块。

- Maven 坐标：`site.addzero:compose-native-component-autocomplet`
- 本地模块路径：`lib/compose/compose-native-component-autocomplet`

## 提供能力

- 旧 API：`site.addzero.component.autocomplet.AddAutoComplete`
- 新迁入 API：`site.addzero.autocomplete.AutoCompleteField`
- 文本建议列表展示
- 焦点驱动的建议项展开与关闭

## 最小示例

```kotlin
var value by remember { mutableStateOf("") }

AutoCompleteField(
    value = value,
    onValueChange = { value = it },
    label = "路径",
    placeholder = "输入关键字",
    getSuggestions = { keyword ->
        listOf(
            AutoCompleteItem(
                id = "1",
                displayText = keyword,
                secondaryText = "候选项",
            )
        )
    },
)
```

## 约束

- 当前模块目录名保留旧拼写 `autocomplet`，避免立即打断仓库内现有依赖
- 后续如果要统一改名，应连同项目路径和依赖方一起迁移

