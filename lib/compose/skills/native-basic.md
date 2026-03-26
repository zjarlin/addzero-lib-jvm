# 原生基础组件索引

## 覆盖模块

- `compose-native-component`
- `compose-native-component-button`
- `compose-native-component-card`
- `compose-native-component-searchbar`
- `compose-native-component-select`
- `compose-native-component-high-level`
- `compose-native-component-assist`

## 适合场景

- 工作台顶部工具栏、搜索栏、筛选栏
- 标准按钮、删除按钮、图标按钮、加载按钮
- 业务卡片、双栏布局、标签页、抽屉、可滚动区域
- 简单选择器、带提示的交互入口、桌面信息密集布局

## 依赖入口

```kotlin
implementation(projects.lib.compose.composeNativeComponent)
implementation(projects.lib.compose.composeNativeComponentButton)
implementation(projects.lib.compose.composeNativeComponentCard)
implementation(projects.lib.compose.composeNativeComponentSearchbar)
implementation(projects.lib.compose.composeNativeComponentSelect)
implementation(projects.lib.compose.composeNativeComponentHighLevel)
```

## 组件速查

### 基础操作

- `AddButton`：标准文本按钮，可带图标、渐变背景和颜色覆盖
- `AddLoadingButton`：执行异步动作时显示加载态
- `AddIconButton`：图标为主的轻量按钮
- `AddDeleteButton`：直接给删除场景用
- `AddEditDeleteButton`：表格行操作的现成组合
- `AddFloatingActionButton`：悬浮主操作入口

### 展示与文本

- `AddCard`：业务卡片主入口
- `BeautifulText`：强调文本展示
- `H1` / `H2` / `H3` / `H4` / `BodyLarge` / `BodyMedium` / `BodySmall` / `Caption`
- `AddMessageToast` + `AddToastListener`：消息提示

### 搜索与选择

- `AddSearchBar`：搜索关键字输入 + 左右插槽
- `AddSelect<T>`：下拉选择
- `AddAutoComplete<T>`：自动补全输入

### 布局与容器

- `AddTabs` / `CustomTabRow`：标签切换
- `AddDrawer`：右侧抽屉式内容区
- `AddDoubleCardLayout`：双卡片并排布局
- `AddLazyList`：统一列表渲染入口
- `MultiColumnContainer` / `AddMultiColumnContainer`：多列容器
- `ScrollableContainer`：统一滚动包裹
- `AddTooltipBox`：图标和紧凑入口建议默认包上它

## 最小组合示例

```kotlin
@Composable
fun DeviceToolbar(
    keyword: String,
    onKeywordChange: (String) -> Unit,
    onSearch: () -> Unit,
    onCreate: () -> Unit,
) {
    AddSearchBar(
        keyword = keyword,
        onKeyWordChanged = onKeywordChange,
        onSearch = onSearch,
        leftSloat = {
            AddButton(displayName = "新建设备", onClick = onCreate)
        }
    )
}
```

```kotlin
@Composable
fun OverviewScreen() {
    AddTabs(
        tabs = listOf(
            TabItem("概览") { AddCard(title = "概览") {} },
            TabItem("日志") { ScrollableContainer { BodyMedium("日志内容") } },
        )
    )
}
```

## 使用原则

- 高密度桌面界面优先组合 `AddSearchBar`、`AddButton`、`AddCard`、`AddTabs`
- 纯图标入口默认再包一层 `AddTooltipBox`
- 如果是表格、树、表单场景，不要在这里硬拼，直接切到对应索引
