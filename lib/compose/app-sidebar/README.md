# app-sidebar

低心智负担的 Compose Multiplatform 应用侧边栏组件库，核心只负责树结构、搜索、插槽和选中态，不再内建固定皮肤。样式统一走外部 `AppSidebarStyleConfig` SPI。

- Maven coordinate: `site.addzero:app-sidebar`
- Local module path: `lib/compose/app-sidebar`

## Features

- 树形导航
- 内建搜索过滤
- 保留输入顺序
- 头部 / 底部插槽
- 行级 `leading` / `label` / `trailing` 插槽
- 无头结构 + 外部样式 SPI
- `commonMain` 可复用

## Usage

先引入默认 adapter：

```kotlin
implementation("site.addzero:app-sidebar")
implementation("site.addzero:app-sidebar-shadcn-adapter")
```

```kotlin
val sidebarState = rememberAppSidebarState(initialSelectedId = "dashboard")
val sections: List<ProjectSection> = loadProjectSections()
val sidebarStyle = rememberShadcnAppSidebarStyleConfig()

WorkbenchScaffold(
    defaultSidebarRatio = 0.22f,
    outerPadding = PaddingValues(16.dp),
    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
    detailPadding = PaddingValues(16.dp),
    sidebar = {
        AppSidebar(
            title = "Workbench",
            items = sections,
            itemId = ProjectSection::id,
            label = ProjectSection::name,
            style = sidebarStyle,
            children = ProjectSection::children,
            state = sidebarState,
            config = appSidebarConfig(
                supportText = "一个拿来就能用的通用应用侧栏。",
            ),
            icon = ProjectSection::icon,
            slots = appSidebarSlots(
                trailing = { section, _, _ ->
                    section.badge?.let { badge ->
                        Text(badge)
                    }
                },
            ),
        )
    },
    contentHeader = {
        WorkbenchToolbar()
    },
    content = {
        WorkbenchContent()
    },
    detail = {
        InspectorPanel()
    },
)
```

## Admin Usage

```kotlin
val adminNodes: List<AdminNode> = loadAdminTree()
val sidebarStyle = rememberShadcnAppSidebarStyleConfig(
    variant = ShadcnAppSidebarVariant.FlushWorkbench,
)

AdminWorkbenchScaffold(
    sidebar = {
        AppSidebar(
            title = "Admin",
            items = adminNodes,
            itemId = AdminNode::id,
            label = AdminNode::title,
            style = sidebarStyle,
            children = AdminNode::children,
            config = appSidebarConfig(
                supportText = "后台工作台",
            ),
            icon = AdminNode::icon,
        )
    },
    content = {
        MemberTable()
    },
    page = adminWorkbenchPageConfig(
        breadcrumb = listOf("系统管理", "用户中心"),
        pageTitle = "成员管理",
        pageSubtitle = "把页面动作和全局工具动作收进统一后台骨架。",
    ),
    config = adminWorkbenchConfig(
        isDarkTheme = isDarkTheme,
        sidebarVisible = sidebarVisible,
        onSidebarToggle = onSidebarToggle,
    ),
    slots = adminWorkbenchSlots(
        pageActions = {
            FilterChip()
            PrimaryActionButton()
        },
        detail = {
            MemberInspector()
        },
        topBarActions = {
            WorkbenchLanguageButton(
                label = currentLanguageLabel,
                onClick = onLanguageClick,
            )
            WorkbenchThemeToggleButton(
                isDarkTheme = isDarkTheme,
                onClick = onThemeToggle,
            )
            WorkbenchNotificationButton(
                count = notificationCount,
                onClick = onNotificationsClick,
            )
            WorkbenchUserButton(
                label = currentUserLabel,
                onClick = onUserClick,
            )
        },
    ),
)
```

## Preview

- 独立预览宿主：`/Users/zjarlin/IdeaProjects/vibepocket/apps/liquiddemo`
- 本地运行：`./gradlew :apps:liquiddemo:jvmRun`

## Notes

- 核心模块不再内建视觉真相，应用层应显式传入样式 adapter
- 推荐直接使用 `site.addzero:app-sidebar-shadcn-adapter`
- 可序列化参数统一收进 `AppSidebarConfig`
- 事件统一收进 `AppSidebarEvents`
- 插槽统一收进 `AppSidebarSlots`
- 搜索状态字段统一命名为 `keyword`
- `AppSidebarScaffold` / `WorkbenchScaffold` 默认都是无缝布局
- `WorkbenchScaffold` 适合“侧栏 + 顶部工具栏 + 主内容 + 右侧详情栏”
- `AdminWorkbenchScaffold` 是后台管理版高阶封装，默认头部固定为“面包屑 / 标题 / 副标题 + 页面动作 + 顶栏动作插槽”
- 只要给 `adminWorkbenchConfig` 传 `onSidebarToggle`，顶栏左侧就会自动出现内置的“隐藏菜单 / 显示菜单”按钮
- 顶栏动作完全由应用层通过 `topBarActions` 组合，库只提供 `Workbench*Button` 这类可复用按钮外观
- `WorkbenchScaffold` 会按宽度自适应：宽窗口三栏，窄一些自动折叠成双栏
- `rememberAppSidebarState` / `rememberWorkbenchScaffoldState` 都基于 Compose `rememberSaveable`
- 左侧栏默认支持拖拽调宽，同时保留默认比例
- 如果业务需要受控选中态，直接读写 `AppSidebarState.selectedId`
- 如果业务已经形成自己的导航树模型，优先在业务模块内部渲染侧栏，不要再把具体菜单实现抽回通用壳层模块
