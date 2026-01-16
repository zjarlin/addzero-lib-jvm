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

## 快速开始

### 环境配置

设置 Suno API Token 环境变量：

```bash
export SUNO_API_TOKEN="your_token_here"
```

### 1. 根据歌名生成音乐

```kotlin
// 搜索歌词并生成音乐
val taskId = MusicDesignUtil.remixBySongName(
    songName = "晴天",
    artistName = "周杰伦",
    tags = "pop, chinese"
)

println("任务 ID: $taskId")
```

### 2. 根据歌名生成音乐（等待完成）

```kotlin
// 搜索歌词并生成音乐，等待完成
val task = MusicDesignUtil.remixBySongNameAndWait(
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
// 根据歌词片段搜索并生成
val taskId = MusicDesignUtil.remixByLyricFragment(
    lyricFragment = "刮风这天我试过握着你手",
    tags = "ballad, emotional"
)

println("任务 ID: $taskId")
```

### 4. 使用自定义歌词生成音乐

```kotlin
val lyrics = """
    天空灰得像哭过
    离开你以后
    并没有更自由
    酸酸的空气
    嗅出我们的距离
""".trimIndent()

val taskId = MusicDesignUtil.createMusicWithLyrics(
    lyrics = lyrics,
    title = "我的歌曲",
    tags = "pop, sad, chinese"
)
```

## API 接口

### 基于歌名生成

#### remixBySongName

根据歌名搜索歌词并生成新歌曲

```kotlin
fun remixBySongName(
    songName: String,           // 歌名
    artistName: String? = null, // 歌手名（可选）
    tags: String = "",          // 音乐风格标签
    model: String = "chirp-v5"  // Suno 模型版本
): String?                      // 返回任务 ID
```

#### remixBySongNameAndWait

根据歌名搜索歌词并生成新歌曲（等待完成）

```kotlin
fun remixBySongNameAndWait(
    songName: String,
    artistName: String? = null,
    tags: String = "",
    model: String = "chirp-v5",
    maxWaitTimeSeconds: Int = 600
): SunoTask?
```

### 基于歌词片段生成

#### remixByLyricFragment

根据歌词片段搜索并生成新歌曲

```kotlin
fun remixByLyricFragment(
    lyricFragment: String,      // 歌词片段
    tags: String = "",          // 音乐风格标签
    model: String = "chirp-v5"  // Suno 模型版本
): String?                      // 返回任务 ID
```

#### remixByLyricFragmentAndWait

根据歌词片段搜索并生成新歌曲（等待完成）

```kotlin
fun remixByLyricFragmentAndWait(
    lyricFragment: String,
    tags: String = "",
    model: String = "chirp-v5",
    maxWaitTimeSeconds: Int = 600
): SunoTask?
```

### 自定义歌词生成

#### createMusicWithLyrics

使用现有歌词生成音乐

```kotlin
fun createMusicWithLyrics(
    lyrics: String,             // 歌词内容
    title: String = "",         // 歌曲标题
    tags: String = "",          // 音乐风格标签
    model: String = "chirp-v5"  // Suno 模型版本
): String                       // 返回任务 ID
```

#### createMusicWithLyricsAndWait

使用现有歌词生成音乐（等待完成）

```kotlin
fun createMusicWithLyricsAndWait(
    lyrics: String,
    title: String = "",
    tags: String = "",
    model: String = "chirp-v5",
    maxWaitTimeSeconds: Int = 600
): SunoTask
```

### 批量生成

#### batchRemixBySongNames

批量根据歌名生成音乐

```kotlin
fun batchRemixBySongNames(
    songInfos: List<Pair<String, String?>>, // 歌曲信息列表（歌名 to 歌手名）
    tags: String = "",
    model: String = "chirp-v5"
): List<String>                             // 返回任务 ID 列表
```

#### batchRemixBySongNamesAndWait

批量根据歌名生成音乐（等待完成）

```kotlin
fun batchRemixBySongNamesAndWait(
    songInfos: List<Pair<String, String?>>,
    tags: String = "",
    model: String = "chirp-v5",
    maxWaitTimeSeconds: Int = 600
): List<SunoTask>
```

### 预览功能

#### previewSongLyrics

搜索歌曲并获取歌词（用于预览）

```kotlin
fun previewSongLyrics(
    songName: String,
    artistName: String? = null
): SongWithLyric?
```

#### previewByLyricFragment

根据歌词片段搜索歌曲（用于预览）

```kotlin
fun previewByLyricFragment(
    lyricFragment: String,
    limit: Int = 5
): List<SongWithLyric>
```

## 完整示例

### 示例 1: 翻唱经典歌曲

```kotlin
fun main() {
    // 1. 预览歌词
    val preview = MusicDesignUtil.previewSongLyrics("晴天", "周杰伦")
    println("原歌曲: ${preview?.song?.name}")
    println("歌词预览: ${preview?.lyric.lrc?.lyric?.take(200)}")
    
    // 2. 生成新版本
    val task = MusicDesignUtil.remixBySongNameAndWait(
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
    val taskId = MusicDesignUtil.remixByLyricFragment(
        lyricFragment = "刮风这天我试过握着你手",
        tags = "ballad, emotional, piano"
    )
    
    println("任务已创建: $taskId")
}
```

### 示例 3: 批量生成音乐

```kotlin
fun main() {
    // 批量生成周杰伦的歌曲新版本
    val songList = listOf(
        "晴天" to "周杰伦",
        "稻香" to "周杰伦",
        "七里香" to "周杰伦"
    )
    
    val tasks = MusicDesignUtil.batchRemixBySongNamesAndWait(
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
    
    val task = MusicDesignUtil.createMusicWithLyricsAndWait(
        lyrics = myLyrics,
        title = "雨天的思念",
        tags = "ballad, sad, piano, chinese",
        maxWaitTimeSeconds = 600
    )
    
    println("创作完成!")
    println("音频: ${task.audioUrl}")
}
```

## 音乐风格标签 (Tags)

常用标签示例：

### 曲风类型
- `pop` - 流行
- `rock` - 摇滚
- `ballad` - 抒情
- `rap` - 说唱
- `electronic` - 电子
- `jazz` - 爵士
- `folk` - 民谣
- `classical` - 古典

### 情感氛围
- `happy` - 快乐
- `sad` - 悲伤
- `emotional` - 感性
- `energetic` - 充满活力
- `calm` - 平静
- `romantic` - 浪漫

### 乐器
- `piano` - 钢琴
- `guitar` - 吉他
- `acoustic` - 原声
- `orchestral` - 管弦乐

### 语言/地区
- `chinese` - 中文
- `english` - 英文
- `japanese` - 日文
- `korean` - 韩文

## 工作流程

1. **搜索歌词**: 从网易云音乐搜索歌曲歌词
2. **清理格式**: 自动去除时间轴标记 `[00:00.00]`
3. **生成音乐**: 调用 Suno API 生成新音乐
4. **等待完成**: 可选择等待生成完成并获取结果

## 注意事项

1. **API Token**: 需要设置 `SUNO_API_TOKEN` 环境变量
2. **歌词版权**: 仅供学习和个人使用
3. **生成时间**: 音乐生成通常需要 1-5 分钟
4. **请求频率**: 注意控制请求频率，避免被限流
5. **歌词清理**: 自动移除时间轴，保留纯文本歌词

## 依赖模块

- `tool-api-music-search` - 网易云音乐搜索
- `tool-api-suno` - VectorEngine Suno API

## 使用场景

- 🎵 音乐翻唱/改编
- 🎤 AI 歌手训练
- 📝 歌词创作辅助
- 🎼 音乐风格转换
- 🎧 个性化音乐生成
- 📊 音乐数据分析

## License

仅供学习和个人使用，请遵守相关版权法律。
