# compose-workbench-shell

KCloud 当前工作台壳层中沉淀出来的通用 Compose shell 组件。

- Maven coordinate: `site.addzero:compose-workbench-shell`
- Local module path: `lib/compose/compose-workbench-shell`

## Scope

- `WorkbenchMetrics` / `WorkbenchPresets`
- `WorkbenchSceneTabs`
- `WorkbenchTreeSidebar`
- `WorkbenchContentSurface`
- `WorkbenchTopBarActionContributor`
- `WorkbenchTopBarActionsHost`

## Usage

```kotlin
val metrics = WorkbenchPresets.DesktopCompact

CompositionLocalProvider(LocalWorkbenchMetrics provides metrics) {
    WorkbenchContentSurface(
        modifier = Modifier.fillMaxSize(),
    ) {
        Screen()
    }
}
```

## Notes

- 不携带 KCloud 专属路由模型
- 树侧栏内部继续复用 `AddTree` 与 `AddSearchBar`
- 顶栏动作只提供可插拔贡献接口，不直接绑定 IoC
