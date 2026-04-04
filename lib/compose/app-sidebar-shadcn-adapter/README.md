# app-sidebar-shadcn-adapter

`app-sidebar` 的默认 shadcn 样式 adapter。

- Maven coordinate: `site.addzero:app-sidebar-shadcn-adapter`
- Local module path: `lib/compose/app-sidebar-shadcn-adapter`

## Usage

```kotlin
implementation("site.addzero:app-sidebar")
implementation("site.addzero:app-sidebar-shadcn-adapter")
```

```kotlin
ShadcnTheme {
    val sidebarStyle = rememberShadcnAppSidebarStyleConfig()

    AppSidebar(
        title = "Console",
        items = items,
        itemId = Item::id,
        label = Item::title,
        style = sidebarStyle,
    )
}
```

如果你要无缝贴工作台边缘，直接切到：

```kotlin
val sidebarStyle = rememberShadcnAppSidebarStyleConfig(
    variant = ShadcnAppSidebarVariant.FlushWorkbench,
)
```
