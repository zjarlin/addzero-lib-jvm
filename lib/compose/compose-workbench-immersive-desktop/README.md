# compose-workbench-immersive-desktop

Compose Desktop 工作台窗口宿主增强模块，当前主要封装 macOS 沉浸式标题栏与 `WorkbenchWindowFrame` 注入。

- Maven coordinate: `site.addzero:compose-workbench-immersive-desktop`
- Local module path: `lib/compose/compose-workbench-immersive-desktop`

## Usage

```kotlin
configureImmersiveDesktopRuntime()

Window(
    onCloseRequest = ::exitApplication,
) {
    ProvideMacOsImmersiveDesktopWindowFrame(
        state = windowState,
        config = MacOsImmersiveDesktopWindowConfig(
            topBarHeight = 44.dp,
            leadingInset = 72.dp,
        ),
    ) {
        App()
    }
}
```

## Notes

- 当前实现只在 macOS 非全屏窗口下启用沉浸式标题栏
- 默认兼容 Compose Desktop + Skiko 的标题栏隐藏方案
- 会向 `LocalWorkbenchWindowFrame` 注入沉浸式顶栏尺寸信息
