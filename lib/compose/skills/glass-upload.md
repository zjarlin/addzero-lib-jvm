# 玻璃态与上传组件索引

## 覆盖模块

- `compose-native-component-glass`
- `compose-klibs-component`

## 适合场景

- 展示型面板、玻璃态工作台、霓虹卡片
- 文件上传、附件选择、上传队列与进度展示

## 依赖入口

```kotlin
implementation(projects.lib.compose.composeNativeComponentGlass)
implementation(projects.lib.compose.composeKlibsComponent)
```

## 组件速查

### 玻璃态视觉

- `GlassButton`
- `NeonGlassButton`
- `LiquidGlassButton`
- `GlassCard`
- `NeonGlassCard`
- `LiquidGlassCard`
- `GlassInfoCard`
- `GlassStatCard`
- `GlassFeatureCard`
- `GlassSidebar`
- `CompactGlassSidebar`
- `GlassTextField`
- `GlassSearchField`
- `GlassTextArea`
- `GlassEffect`
- `Modifier.glassEffect()`
- `Modifier.neonGlassEffect()`
- `Modifier.liquidGlassEffect()`

### 文件与上传

- `AddFileUploader`
- `AddFilePicker`
- `AddMultiFilePicker`
- `UploadManager`
- `UploadManagerUI`
- `GlobalUploadManager`

## 最小组合示例

```kotlin
@Composable
fun UploadPanel() {
    GlassCard {
        Column {
            AddFileUploader(onUploadFinished = { files -> })
            UploadManagerUI(uploadManager = GlobalUploadManager.manager)
        }
    }
}
```

## 使用原则

- 工具型后台默认不要整页都玻璃态，只在重点卡片或展示区局部使用
- 需要统一上传队列时优先复用 `UploadManager`
- 文件选取、上传结果和进度区不要自己从零开始拼
