# liquid-glass

Apple 风格液态玻璃 Compose Multiplatform 组件库，提供开箱即用的按钮、卡片、树形侧边栏和液态材质宿主。

- Maven coordinate: `site.addzero:liquid-glass`
- Local module path: `lib/compose/liquid-glass`

## Components

- `LiquidGlassRoot`
- `LiquidGlassAppTheme`
- `LiquidGlassButton`
- `LiquidGlassIconButton`
- `LiquidGlassCard`
- `LiquidGlassWorkbenchRoot`
- `LiquidGlassWorkbenchDefaults`
- `LiquidGlassTabs`
- `LiquidGlassSidebarMenu`
- `LiquidButton`
- `LiquidBottomTab`
- `LiquidBottomTabs`
- `LiquidSlider`
- `LiquidToggle`

## Usage

```kotlin
import site.addzero.liquidglass.LiquidGlassAppTheme
import site.addzero.liquidglass.LiquidGlassButton
import site.addzero.liquidglass.LiquidGlassCard
import site.addzero.liquidglass.LiquidGlassCardHeader
import site.addzero.liquidglass.LiquidGlassWorkbenchDefaults
import site.addzero.liquidglass.LiquidGlassWorkbenchRoot

LiquidGlassAppTheme {
    val items: List<ProjectNode> = loadProjectNodes()

    LiquidGlassWorkbenchRoot(
        wallpaper = {
            // 可选壁纸或背景图
        },
    ) {
        LiquidGlassSidebarMenu(
            title = "Project",
            items = items,
            selectedId = "dashboard",
            itemId = ProjectNode::id,
            label = ProjectNode::title,
            subtitle = ProjectNode::subtitle,
            icon = ProjectNode::icon,
            children = ProjectNode::children,
            onSelect = { node ->
                navigateTo(node.id)
            },
        )

        LiquidGlassCard(
            spec = LiquidGlassWorkbenchDefaults.section,
        ) {
            LiquidGlassCardHeader(
                title = "Liquid Glass",
                subtitle = "Compose 组件库",
            )
            LiquidGlassButton(
                text = "Primary",
                onClick = {},
            )
        }
    }
}
```

## Notes

- 直接依赖官方 `io.github.kyant0:backdrop`
- 目前依赖 `io.github.fletchmckee.liquid:liquid`
- 额外依赖 `io.github.kyant0:shapes`，用于迁入 AndroidLiquidGlass 风格控制组件
- 组件代码放在 `commonMain`，适合 Compose Multiplatform 复用
- `LiquidGlassWorkbenchDefaults` 提供一套更适合桌面工作台的蓝色紧凑材质参数，可直接拿来做侧边栏、内容区、小指标卡和胶囊标签
