# compose-zh-fonts

`Compose Multiplatform` 中文字体支持库，重点解决 `wasmJs` 场景下的中文字体加载问题。

- Maven coordinate: `site.addzero:compose-zh-fonts`
- Local module path: `lib/compose/compose-zh-fonts`

## Current Strategy

- `jvmMain` 直接使用内置的 `Noto Sans CJK SC Regular`
- `wasmJsMain` 优先尝试通过浏览器 `queryLocalFonts()` 读取设备字体，并用 `FontFamily.Resolver.preload(...)` 预加载
- 如果 `queryLocalFonts()` 不可用、权限未授予、或者没有匹配到可用中文字体，再回退到内置 `Noto Sans CJK SC Regular`

当前 wasm 端内置优先匹配这些设备字体：

- `PingFang SC`
- `STHeiti`
- `Microsoft YaHei UI`
- `Microsoft YaHei`

## Add Dependency

```kotlin
dependencies {
    implementation(project(":lib:compose:compose-zh-fonts"))
}
```

## Basic Usage

直接替换 `MaterialTheme.typography`：

```kotlin
import androidx.compose.material3.MaterialTheme
import site.addzero.compose.zh.fonts.rememberChineseTypography

MaterialTheme(
    typography = rememberChineseTypography(),
) {
    App()
}
```

这适合大多数场景。
字体未完成加载时会先使用原始 `Typography`，加载完成后自动切换到中文字体。

## Strict Usage

如果你不接受首屏先用默认字体，再切换到中文字体，改用 `rememberChineseTypographyOrNull()`：

```kotlin
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import site.addzero.compose.zh.fonts.rememberChineseTypographyOrNull

val typography = rememberChineseTypographyOrNull()
if (typography == null) {
    Text("字体加载中")
} else {
    MaterialTheme(typography = typography) {
        App()
    }
}
```

这个模式更适合 `wasmJs` / `WebView` 首屏必须避免中文闪烁或方块字的场景。

## Wasm Notes

- 设备字体读取依赖浏览器提供 `queryLocalFonts()`
- 浏览器不支持这个 API 时，会自动回退到内置字体
- 浏览器要求授权访问本机字体时，用户需要允许，否则也会回退到内置字体
- 由于仍然保留内置字体兜底，`wasmJs` 构建产物体积不会因为接入设备字体而消失

## API Surface

对外 API 只有这几个：

```kotlin
@Composable
fun rememberChineseUiFontFamilyOrNull(): FontFamily?

@Composable
fun rememberChineseTypography(base: Typography = Typography()): Typography

@Composable
fun rememberChineseTypographyOrNull(base: Typography = Typography()): Typography?
```

## Verification

推荐至少验证这几件事：

- `jvm` 下中文是否正常显示
- `wasmJs` 下支持 `queryLocalFonts()` 的浏览器是否优先使用设备字体
- 未授权或不支持 `queryLocalFonts()` 的浏览器是否回退到内置字体
