# app-sidebar

低心智负担的 Compose Multiplatform 应用侧边栏组件库，默认内建树结构、搜索、插槽和暗色专业风格。侧边栏 API 直接接业务节点，不再要求额外定义 `SidebarItem` 包装实体。

- Maven coordinate: `site.addzero:app-sidebar`
- Local module path: `lib/compose/app-sidebar`

## Features

- 树形导航
- 内建搜索过滤
- 保留输入顺序
- 头部 / 底部插槽
- 行级 `leading` / `label` / `trailing` 插槽
- 默认暗色专业风格
- `commonMain` 可复用

## Usage

```kotlin
val sidebarState = rememberAppSidebarState(initialSelectedId = "dashboard")
val sections: List<ProjectSection> = loadProjectSections()

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
            children = ProjectSection::children,
            state = sidebarState,
            config = AppSidebarConfig(
                supportText = "一个拿来就能用的通用应用侧栏。",
            ),
            icon = ProjectSection::icon,
            slots = AppSidebarSlots(
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

AdminWorkbenchScaffold(
    breadcrumb = listOf("系统管理", "用户中心"),
    pageTitle = "成员管理",
    pageSubtitle = "把页面动作和全局工具动作收进统一后台骨架。",
    sidebar = {
        AppSidebar(
            title = "Admin",
            items = adminNodes,
            itemId = AdminNode::id,
            label = AdminNode::title,
            children = AdminNode::children,
            config = AppSidebarConfig(
                supportText = "后台工作台",
            ),
            icon = AdminNode::icon,
        )
    },
    pageActions = {
        FilterChip()
        PrimaryActionButton()
    },
    content = {
        MemberTable()
    },
    detail = {
        MemberInspector()
    },
    onGlobalSearchClick = onGlobalSearchClick,
    languageLabel = currentLanguageLabel,
    onLanguageClick = onLanguageClick,
    isDarkTheme = isDarkTheme,
    onThemeToggle = onThemeToggle,
    notificationCount = notificationCount,
    onNotificationsClick = onNotificationsClick,
    userLabel = currentUserLabel,
    onUserClick = onUserClick,
)
```

## Preview

- 独立预览宿主：`/Users/zjarlin/IdeaProjects/vibepocket/apps/liquiddemo`
- 本地运行：`./gradlew :apps:liquiddemo:jvmRun`

## Notes

- 默认视觉参数已经写死成更适合商用的暗色桌面风格
- 可序列化参数统一收进 `AppSidebarConfig`
- 事件统一收进 `AppSidebarEvents`
- 插槽统一收进 `AppSidebarSlots`
- 搜索状态字段统一命名为 `keyword`
- `AppSidebarScaffold` / `WorkbenchScaffold` 默认都是无缝布局
- `WorkbenchScaffold` 适合“侧栏 + 顶部工具栏 + 主内容 + 右侧详情栏”
- `AdminWorkbenchScaffold` 是后台管理版高阶封装，默认头部固定为“面包屑 / 标题 / 副标题 + 页面动作 + 全局工具动作”
- 后台工具动作默认顺序固定为“搜索 / 语言 / 主题 / 通知 / 用户”，未提供的参数不会渲染
- `WorkbenchScaffold` 会按宽度自适应：宽窗口三栏，窄一些自动折叠成双栏
- `rememberAppSidebarState` / `rememberWorkbenchScaffoldState` 都基于 Compose `rememberSaveable`
- 左侧栏默认支持拖拽调宽，同时保留默认比例
- 如果业务需要受控选中态，直接读写 `AppSidebarState.selectedId`
- 如果业务已经形成自己的导航树模型，优先在业务模块内部渲染侧栏，不要再把具体菜单实现抽回通用壳层模块
