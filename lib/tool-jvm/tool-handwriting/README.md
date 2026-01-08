# tool-handwriting

JVM 工具模块，负责把文本渲染为带有手写质感的图片，可运行在任意 Kotlin/Java 项目内。

## 快速上手

```kotlin
val options = HandwritingRenderOptions(
    randomSeed = 42L,
    maxContentWidth = 1024,
    fontSource = HandwritingFontSource.systemDefault(),
)
HandwritingImageTool.writeToFile(
    text = "习惯题写字是很难的，但坚持就会看到效果。",
    output = Paths.get("/tmp/handwriting.png"),
    options = options
)
```

### 自定义字体

```kotlin
val fontBytes = Files.readAllBytes(Paths.get("fonts/MyHandwriting.ttf"))
val customOptions = HandwritingRenderOptions(
    fontSource = HandwritingFontSource.fromBytes(fontBytes),
    textColor = Color(0x30, 0x25, 0x1F)
)
```

> **Note** 按照版权要求请自行引入授权的手写字体。模块默认会尝试 `楷体` 等系统字体，找不到时会降级到 Serif 字体。

## 渲染能力

- 任意 UTF-16 文本自动换行、支持段落缩进。
- 抖动（baseline、横向、角度）可通过 `HandwritingRenderOptions` 调整，支持 `randomSeed` 获取可重复输出。
- 内置纸纹理生成器，可自定义行距、噪声、引导线。
- `HandwritingImageTool.encode` 可以直接返回 PNG/JPG 字节，方便 HTTP 接口或数据库持久化。

详见 `HandwritingImageTool` 与 `HandwritingRenderOptions` KDoc 了解全部参数。
