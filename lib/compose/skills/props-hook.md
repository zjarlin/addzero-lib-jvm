# 注解、Hook 与辅助状态索引

## 覆盖模块

- `compose-props-annotations`
- `compose-props-processor`
- `compose-native-component-hook`
- `compose-native-component-assist`

## 适合场景

- 想给 `@Composable` 自动生成 `State` / `remember` / `Widget` 辅助代码
- 想把下拉选择、自动补全等 UI 状态收束成 Hook 对象
- 想复用自动聚焦、标题图标、统一取 id 等辅助工具

## 依赖入口

```kotlin
implementation(projects.lib.compose.composePropsAnnotations)
implementation(projects.lib.compose.composeNativeComponentHook)
```

```kotlin
dependencies {
    kspCommonMainMetadata(projects.lib.compose.composePropsProcessor)
}
```

## 组件速查

### 注解与生成

- `@ComposeAssist`
- `@AssistExclude`
- `ComposeAttrsProcessor`

### Hook 与状态

- `UseHook<T>`
- `UseSelect<T>`
- `UseAutoComplate<T>`

### 辅助工具

- `AutoFocus()`
- `AddFun.getIdExt`
- `getTitleIcon()`

## 最小组合示例

```kotlin
@Composable
@ComposeAssist
fun DeviceTitle(
    title: String,
    subtitle: String = "",
) {
    Column {
        H3(title)
        Caption(subtitle)
    }
}
```

```kotlin
@Composable
fun SearchBox() {
    val hook = remember {
        UseSelect(
            label = "状态",
            options = listOf("启用", "停用")
        )
    }
    hook.Render {
        render()
    }
}
```

## 使用原则

- 已经是稳定可复用的 `@Composable`，再考虑加 `@ComposeAssist`
- 只是页面级临时状态，不要为了“看起来高级”硬上注解生成
- `UseSelect` / `UseAutoComplate` 适合把输入状态和渲染入口一起收口
