# compose-native-component-text

Reusable Compose Multiplatform text primitives for Addzero UI, including typography helpers and centered placeholder text like `TodoText`.

Maven coordinate: `site.addzero:compose-native-component-text`

Local module path: `/Users/zjarlin/IdeaProjects/addzero-lib-jvm/lib/compose/compose-native-component-text`

Minimal usage example:

```kotlin
import site.addzero.component.text.TodoText

@Composable
fun DeviceDebugPlaceholder() {
    TodoText(
        title = "MCU 调试",
        description = "后台调试日志与运行时接口当前未开放",
    )
}
```

Platform notes:

- Built as a Compose Multiplatform library with Material 3 text styling.
- The caller should provide a Material-themed composition when custom colors or typography are expected.
