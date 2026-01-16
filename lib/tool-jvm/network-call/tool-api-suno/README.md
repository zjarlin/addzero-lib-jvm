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
- ✅ 环境变量配置

## 快速开始

### 1. 配置 API Token

```bash
export SUNO_API_TOKEN="your-api-token-here"
```

### 2. 使用 SunoUtil（推荐）

```kotlin
// 生成音乐并等待完成
val task = SunoUtil.generateMusicInspirationAndWait(
    description = "一首关于春天的欢快流行歌",
    instrumental = false,
    model = "chirp-v5"
)

println("音频: ${task.audioUrl}")
```

### 3. 使用 SunoClient（高级用法）

```kotlin
val client = SunoClient(apiToken = "your-token")

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
// 方式1：使用 SunoUtil
val taskId = SunoUtil.generateMusicInspiration(
    description = "一首关于夏天的轻快歌曲",
    instrumental = false,  // 是否纯音乐
    model = "chirp-v5"
)

// 方式2：使用 SunoClient
val client = SunoClient(token)
val taskId = client.generateMusicInspiration(
    description = "一首关于夏天的轻快歌曲",
    instrumental = false,
    model = "chirp-v5"
)
```

### 2. 生成音乐（自定义模式）

使用自定义歌词生成音乐。

```kotlin
val lyrics = """
    [Verse]
    春天来了，花儿开了
    阳光明媚，心情愉快
    
    [Chorus]
    让我们一起歌唱
    迎接美好的春天
""".trimIndent()

val taskId = SunoUtil.generateMusicCustom(
    lyrics = lyrics,
    title = "春天的歌",
    tags = "pop, cheerful, spring",
    model = "chirp-v5"
)
```

### 3. 扩展音乐（续写模式）

延长现有音乐，从指定时间点继续创作。

```kotlin
val extendTaskId = SunoUtil.extendMusic(
    clipId = "原音频ID",
    continueAt = 60,  // 从60秒处继续
    lyrics = "[Verse 2]\n继续的歌词内容",
    title = "扩展版标题",
    tags = "pop, uplifting",
    model = "chirp-v5"
)
```

### 4. 生成歌词

单独生成歌词文本。

```kotlin
val lyrics = SunoUtil.generateLyrics("写一首关于秋天的歌词")
println("生成的歌词:\n$lyrics")
```

### 5. 拼接歌曲

将音频片段拼接成完整歌曲。

```kotlin
val taskId = SunoUtil.concatSongs(clipId = "音频片段ID")
```

### 6. 查询任务状态

```kotlin
// 查询单个任务
val task = SunoUtil.fetchTask(taskId)
println("状态: ${task?.status}")
println("音频: ${task?.audioUrl}")

// 批量查询
val tasks = SunoUtil.batchFetchTasks(listOf(taskId1, taskId2, taskId3))
tasks.forEach { task ->
    println("${task.title}: ${task.status}")
}
```

### 7. 等待任务完成

```kotlin
// 等待单个任务
val result = SunoUtil.waitForCompletion(
    taskId = taskId,
    maxWaitTimeSeconds = 600,  // 最长等待10分钟
    pollIntervalSeconds = 10,   // 每10秒查询一次
    onStatusUpdate = { status ->
        println("状态更新: $status")
    }
)

// 等待多个任务
val results = SunoUtil.waitForBatchCompletion(
    taskIds = listOf(taskId1, taskId2),
    maxWaitTimeSeconds = 600
)
```

### 8. 一键生成并等待

```kotlin
// 灵感模式
val task = SunoUtil.generateMusicInspirationAndWait(
    description = "一首轻松的爵士乐",
    instrumental = true,
    model = "chirp-v5"
)

// 自定义模式
val task = SunoUtil.generateMusicCustomAndWait(
    lyrics = "[Verse]\n歌词内容...",
    title = "歌曲标题",
    tags = "jazz, relaxing",
    model = "chirp-v5"
)
```

## 模型版本

- `chirp-v5` - 最新版本（默认）
- `chirp-v4` - 上一代版本
- `chirp-v3-5` - 早期版本

## 任务状态

- `pending` - 等待处理
- `processing` - 处理中
- `complete` - 完成
- `streaming` - 流式完成
- `error` - 失败

## 完整示例

```kotlin
fun main() {
    // 1. 生成音乐（灵感模式）
    val task1 = SunoUtil.generateMusicInspirationAndWait(
        description = "一首怀旧的民谣，关于童年回忆",
        instrumental = false,
        model = "chirp-v5"
    )
    println("生成完成: ${task1.audioUrl}")
    
    // 2. 生成音乐（自定义歌词）
    val lyrics = """
        [Verse]
        回忆中的那个夏天
        我们在树下玩耍
        
        [Chorus]
        时光匆匆流逝
        但记忆永存心间
    """.trimIndent()
    
    val task2 = SunoUtil.generateMusicCustomAndWait(
        lyrics = lyrics,
        title = "童年回忆",
        tags = "folk, nostalgic, acoustic",
        model = "chirp-v5"
    )
    
    // 3. 扩展音乐
    val extendTaskId = SunoUtil.extendMusic(
        clipId = task2.id,
        continueAt = 60,
        lyrics = "[Verse 2]\n继续讲述那些美好时光",
        title = "童年回忆（完整版）",
        tags = "folk, nostalgic"
    )
    
    val extendedTask = SunoUtil.waitForCompletion(extendTaskId)
    println("扩展完成: ${extendedTask.audioUrl}")
    
    // 4. 批量查询
    val allTasks = SunoUtil.batchFetchTasks(listOf(task1.id, task2.id))
    allTasks.forEach { task ->
        println("${task.title}: ${task.duration}秒")
    }
}
```

## 注意事项

1. **API Token**: 必须通过环境变量 `SUNO_API_TOKEN` 配置
2. **生成时间**: 音乐生成通常需要 30-120 秒
3. **轮询机制**: 建议使用 `waitForCompletion` 自动轮询结果
4. **模型选择**: 默认使用 chirp-v5，可根据需要选择其他版本
5. **歌词格式**: 自定义模式支持 `[Verse]`、`[Chorus]` 等标签

## API 文档

VectorEngine 官方文档: https://vectorengine.apifox.cn/

## 依赖

```kotlin
dependencies {
    implementation("com.squareup.okhttp3:okhttp:4.x.x")
    implementation("com.alibaba.fastjson2:fastjson2-kotlin:2.x.x")
}
```

