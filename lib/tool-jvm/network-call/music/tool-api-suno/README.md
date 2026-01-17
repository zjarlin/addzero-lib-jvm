# Suno API Client

完整的 Kotlin VectorEngine Suno API 客户端，支持音乐生成、扩展、歌词生成等功能。

## 功能特性

- ✅ 生成音乐（灵感模式）
- ✅ 生成音乐（自定义模式，带歌词）
- ✅ 扩展现有音乐（续写模式）
- ✅ 生成歌词
- ✅ 拼接歌曲
- ✅ 查询单个任务
- ✅ 批量查询任务
- ✅ 等待任务完成（自动轮询）
- ✅ 支持 chirp-v5 模型（默认）
- ✅ 环境变量/运行动态配置
- ✅ 使用 Kotlin 密封类与 `kotlinx.serialization` 提供完善的类型安全

## 快速开始

### 1. 配置 API Token

你可以设置环境变量：
```bash
export SUNO_API_TOKEN="your-api-token-here"
```

或者在运行时代码中动态配置：
```kotlin
Suno.config(apiKey = "runtime-token", baseUrl = "https://api.vectorengine.ai")
```

### 2. 使用 Suno 对象（推荐）

`Suno` 是一个全局单例入口，默认从环境变量读取配置。

```kotlin
// 生成音乐并等待完成
val task = Suno.waitForCompletion(
    taskId = Suno.generateMusicInspiration(
        description = "一首关于春天的欢快流行歌",
        instrumental = false,
        model = "chirp-v5"
    )
)

println("音频: ${task.audioUrl}")
```

### 3. 使用 SunoClient（高级用法）

如果你需要管理多个不同的 API 账户，可以直接实例化 `SunoClient`。

```kotlin
val client = SunoClient(apiKey = "your-token")

// 生成音乐
val taskId = client.generateMusicInspiration(
    description = "一首怀旧的民谣",
    instrumental = false,
    model = "chirp-v5"
)

// 等待完成
val result = client.waitForCompletion(taskId) { status ->
    println("当前状态: $status")
}

println("音频: ${result.audioUrl}")
```

## API 接口

### 1. 生成音乐（灵感模式）

通过描述生成音乐，AI 自动创作歌词和旋律。

```kotlin
val taskId = Suno.generateMusicInspiration(
    description = "一首关于夏天的轻快歌曲",
    instrumental = false,  // 是否纯音乐
    model = "chirp-v5"
)
```

### 2. 生成音乐（自定义模式）

使用 `SunoSubmitRequest.Custom` 对象或便捷方法。

```kotlin
val lyrics = """
    [Verse]
    春天来了，花儿开了
    阳光明媚，心情愉快
    
    [Chorus]
    让我们一起歌唱
    迎接美好的春天
""".trimIndent()

val taskId = Suno.generateMusicCustom(
    prompt = lyrics,
    title = "春天的歌",
    tags = "pop, cheerful, spring",
    model = "chirp-v5"
)

// 或者使用统一提交接口
val taskId = Suno.submitMusic(
    SunoSubmitRequest.Custom(
        prompt = lyrics,
        title = "春天的歌",
        tags = "pop, cheerful, spring",
        mv = "chirp-v5"
    )
)
```

### 3. 扩展音乐（续写模式）

延长现有音乐，从指定时间点继续创作。

```kotlin
val extendTaskId = Suno.extendMusic(
    clipId = "原音频ID",
    continueAt = 60,  // 从60秒处继续
    prompt = "[Verse 2]\n继续的歌词内容",
    title = "扩展版标题",
    tags = "pop, uplifting",
    model = "chirp-v5"
)
```

### 4. 生成歌词

```kotlin
val lyrics = Suno.generateLyrics("写一首关于秋天的歌词")
```

### 5. 拼接歌曲

```kotlin
val taskId = Suno.concatSongs(clipId = "音频片段ID")
```

## 依赖

本项目基于 Kotlin 1.9+，集成了以下库：

```kotlin
dependencies {
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")
}
```

推荐使用项目内部的 `jvm-json` 插件自动配置。
