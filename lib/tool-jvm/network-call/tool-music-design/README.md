# 音乐设计工具 (Music Design)

整合网易云音乐歌词搜索和 VectorEngine Suno AI 音乐生成功能，实现基于现有歌词的音乐创作。

## 功能特性

- ✅ 根据歌名搜索歌词并生成新歌曲
- ✅ 根据歌词片段搜索并生成音乐
- ✅ 使用自定义歌词生成音乐
- ✅ 批量生成音乐
- ✅ 自动清理歌词时间轴
- ✅ 支持同步/异步模式
- ✅ 歌词预览功能
- ✅ 支持传入 API Token 或使用环境变量

## 快速开始

### 环境配置

方式 1: 设置环境变量

```bash
export SUNO_API_TOKEN="your_token_here"
```

方式 2: 代码中传入 Token

```kotlin
val musicDesign = MusicDesignUtil(apiToken = "your_token_here")
```

### 1. 根据歌名生成音乐

```kotlin
// 创建实例
val musicDesign = MusicDesignUtil() // 从环境变量读取 token

// 搜索歌词并生成音乐
val taskId = musicDesign.remixBySongName(
    songName = "晴天",
    artistName = "周杰伦",
    tags = "pop, chinese"
)

println("任务 ID: $taskId")
```

### 2. 根据歌名生成音乐（等待完成）

```kotlin
val musicDesign = MusicDesignUtil()

// 搜索歌词并生成音乐，等待完成
val task = musicDesign.remixBySongNameAndWait(
    songName = "晴天",
    artistName = "周杰伦",
    tags = "pop, chinese, ballad",
    maxWaitTimeSeconds = 600
)

println("音频 URL: ${task.audioUrl}")
println("视频 URL: ${task.videoUrl}")
```

### 3. 根据歌词片段生成音乐

```kotlin
val musicDesign = MusicDesignUtil()

// 根据歌词片段搜索并生成
val taskId = musicDesign.remixByLyricFragment(
    lyricFragment = "刮风这天我试过握着你手",
    tags = "ballad, emotional"
)

println("任务 ID: $taskId")
```

### 4. 使用自定义歌词生成音乐

```kotlin
val musicDesign = MusicDesignUtil()

val lyrics = """
    天空灰得像哭过
    离开你以后
    并没有更自由
    酸酸的空气
    嗅出我们的距离
""".trimIndent()

val taskId = musicDesign.createMusicWithLyrics(
    lyrics = lyrics,
    title = "我的歌曲",
    tags = "pop, sad, chinese"
)
```

## 完整示例

### 示例 1: 翻唱经典歌曲

```kotlin
fun main() {
    // 1. 预览歌词
    val preview = MusicDesignUtil.previewSongLyrics("晴天", "周杰伦")
    println("原歌曲: ${preview?.song?.name}")
    println("歌词预览: ${preview?.lyric.lrc?.lyric?.take(200)}")
    
    // 2. 创建实例并生成新版本
    val musicDesign = MusicDesignUtil() // 从环境变量读取 token
    val task = musicDesign.remixBySongNameAndWait(
        songName = "晴天",
        artistName = "周杰伦",
        tags = "pop, acoustic, chinese",
        maxWaitTimeSeconds = 600
    )
    
    // 3. 获取结果
    if (task != null) {
        println("生成成功!")
        println("音频: ${task.audioUrl}")
        println("视频: ${task.videoUrl}")
        println("标题: ${task.title}")
    }
}
```

### 示例 2: 根据歌词片段创作

```kotlin
fun main() {
    // 1. 搜索歌词片段
    val results = MusicDesignUtil.previewByLyricFragment(
        lyricFragment = "刮风这天我试过握着你手",
        limit = 3
    )
    
    println("找到 ${results.size} 首歌曲:")
    results.forEach { result ->
        println("- ${result.song.name} - ${result.song.artists.joinToString { it.name }}")
    }
    
    // 2. 基于第一首生成新歌
    val musicDesign = MusicDesignUtil()
    val taskId = musicDesign.remixByLyricFragment(
        lyricFragment = "刮风这天我试过握着你手",
        tags = "ballad, emotional, piano"
    )
    
    println("任务已创建: $taskId")
}
```

### 示例 3: 批量生成音乐

```kotlin
fun main() {
    val musicDesign = MusicDesignUtil()
    
    // 批量生成周杰伦的歌曲新版本
    val songList = listOf(
        "晴天" to "周杰伦",
        "稻香" to "周杰伦",
        "七里香" to "周杰伦"
    )
    
    val tasks = musicDesign.batchRemixBySongNamesAndWait(
        songInfos = songList,
        tags = "pop, chinese, acoustic",
        maxWaitTimeSeconds = 900
    )
    
    println("生成完成 ${tasks.size} 首歌曲:")
    tasks.forEach { task ->
        println("- ${task.title}: ${task.audioUrl}")
    }
}
```

### 示例 4: 自定义歌词创作

```kotlin
fun main() {
    val musicDesign = MusicDesignUtil()
    
    val myLyrics = """
        [Verse 1]
        窗外的雨还在下
        思念像藤蔓爬上心头
        你的笑容在记忆里发芽
        却开不出花
        
        [Chorus]
        如果时光能倒流
        我会紧紧握住你的手
        不让遗憾成为永久
        在这个雨天
    """.trimIndent()
    
    val task = musicDesign.createMusicWithLyricsAndWait(
        lyrics = myLyrics,
        title = "雨天的思念",
        tags = "ballad, sad, piano, chinese",
        maxWaitTimeSeconds = 600
    )
    
    println("创作完成!")
    println("音频: ${task.audioUrl}")
}
```

## 注意事项

1. **API Token**: 需要传入 `apiToken` 参数或设置 `SUNO_API_TOKEN` 环境变量
2. **歌词版权**: 仅供学习和个人使用
3. **生成时间**: 音乐生成通常需要 1-5 分钟
4. **请求频率**: 注意控制请求频率，避免被限流
5. **歌词清理**: 自动移除时间轴，保留纯文本歌词

## 依赖模块

- `tool-api-music-search` - 网易云音乐搜索
- `tool-api-suno` - VectorEngine Suno API

## License

仅供学习和个人使用，请遵守相关版权法律。
