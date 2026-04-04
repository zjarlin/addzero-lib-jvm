# scaffold-spi

共享的 Compose Multiplatform 工作台壳层 SPI。

- Maven coordinate: `site.addzero:scaffold-spi`
- Local module path: `lib/compose/scaffold-spi`

## Scope

- `spi/sidebar/SidebarRender.kt`
- `spi/header/HeaderRender.kt`
- `spi/content/ContentRender.kt`
- `spi/scaffolding/ScaffoldingSpi.kt`
- `WorkbenchRenderers.kt`
- `AdminWorkbenchScaffolding.kt`

这个模块只保留壳层聚合和最小渲染边界，不再承载 `Screen`、`ScreenTree`、`ScreenSidebar` 这类具体导航实现。

## Usage

```kotlin
@Single(binds = [ScaffoldingSpi::class])
class MyScaffolding : ScaffoldingSpi

RenderAdminScaffolding(
    scaffolding = koinInject(),
    sidebarVisible = true,
    onSidebarToggle = { /* toggle sidebar */ },
)
```

## Notes

- 具体导航树、菜单模型、搜索与选择逻辑应留在业务模块内部实现
- `scaffold-spi` 只负责把 sidebar / header / content 三块渲染器聚合进工作台壳层
- `ScaffoldingSpi` 提供更高一层的“整页骨架”抽象，业务只需要覆写自己关心的槽位
- 这个模块仍然 `api` 导出 `app-sidebar`，因为 `WorkbenchScaffold` 及其壳层状态仍由这里统一复用
