# 音乐搜索 API Client

基于网易云音乐的 Kotlin 音乐搜索客户端，支持歌曲、歌手、专辑、歌词等多种搜索方式。

## 功能特性

- ✅ 搜索歌曲
- ✅ 搜索歌手
- ✅ 搜索专辑
- ✅ 搜索歌单
- ✅ 根据歌名+歌手搜索
- ✅ 根据歌词片段搜索
- ✅ 获取歌词（原文+翻译）
- ✅ 获取歌曲详情
- ✅ 批量查询
- ✅ 无需 API Token

## 快速开始

### 1. 搜索歌曲

```kotlin
// 简单搜索
val songs = MusicSearchUtil.searchSongs("晴天", limit = 10)
songs.forEach { song ->
    val artists = song.artists.joinToString(", ") { it.name }
    println("${song.name} - $artists")
}
```

### 2. 根据歌名和歌手搜索

```kotlin
// 精确搜索
val songs = MusicSearchUtil.searchBySongAndArtist(
    songName = "晴天",
    artistName = "周杰伦"
)

songs.forEach { song ->
    println("${song.name} - ${song.album.name}")
    println("时长: ${song.duration / 1000}秒")
}
```

### 3. 获取歌词

```kotlin
val lyricResponse = MusicSearchUtil.getLyric(songId = 186016)

// 原文歌词
println(lyricResponse.lrc?.lyric)

// 翻译歌词
println(lyricResponse.tlyric?.lyric)

// 罗马音歌词
println(lyricResponse.romalrc?.lyric)
```

## API 接口

### 搜索相关

#### 搜索歌曲

```kotlin
// 方式1：使用 MusicSearchUtil
val songs = MusicSearchUtil.searchSongs("关键词", limit = 30)

// 方式2：使用 MusicSearchClient
val client = MusicSearchClient()
val songs = client.searchSongs("关键词", limit = 30, offset = 0)
```

#### 搜索歌手

```kotlin
val artists = MusicSearchUtil.searchArtists("周杰伦", limit = 10)
artists.forEach { artist ->
    println("${artist.name}")
    println("  专辑数: ${artist.albumSize}")
    println("  歌曲数: ${artist.musicSize}")
}
```

#### 搜索专辑

```kotlin
val albums = MusicSearchUtil.searchAlbums("叶惠美", limit = 10)
albums.forEach { album ->
    println("${album.name} - ${album.artist?.name}")
    println("  歌曲数: ${album.size}")
}
```

#### 搜索歌单

```kotlin
val playlists = MusicSearchUtil.searchPlaylists("华语经典", limit = 10)
playlists.forEach { playlist ->
    println("${playlist.name}")
    println("  创建者: ${playlist.creator?.nickname}")
    println("  播放量: ${playlist.playCount}")
}
```

### 高级搜索

#### 根据歌名+歌手搜索

```kotlin
val songs = MusicSearchUtil.searchBySongAndArtist(
    songName = "稻香",
    artistName = "周杰伦"  // 可选，不传则只按歌名搜索
)
```

#### 根据歌词搜索

```kotlin
val songs = MusicSearchUtil.searchByLyric("刮风这天我试过握着你手")
songs.forEach { song ->
    val artists = song.artists.joinToString(", ") { it.name }
    println("${song.name} - $artists")
}
```

### 歌词相关

#### 获取歌词（通过歌曲 ID）

```kotlin
val lyricResponse = MusicSearchUtil.getLyric(songId)

// 原文歌词
val originalLyric = lyricResponse.lrc?.lyric

// 翻译歌词（如果有）
val translatedLyric = lyricResponse.tlyric?.lyric

// 罗马音歌词（如果有）
val romaLyric = lyricResponse.romalrc?.lyric
```

#### 获取歌词（通过歌名）

```kotlin
// 根据歌名获取歌词
val lyric = MusicSearchUtil.getLyric("晴天", "周杰伦")

// 只传歌名（会返回第一个匹配结果）
val lyric = MusicSearchUtil.getLyric("晴天")

println(lyric?.lrc?.lyric)
```

#### 获取歌词（通过歌词片段）

```kotlin
// 根据歌词片段获取完整歌词（返回多个匹配结果）
val results = MusicSearchUtil.getLyricsByFragment("刮风这天我试过握着你手", limit = 3)

results.forEach { result ->
    println("歌曲: ${result.song.name}")
    println("歌手: ${result.song.artists.joinToString { it.name }}")
    println("完整歌词:")
    println(result.lyric.lrc?.lyric)
    println("---")
}
```

### 歌曲详情

#### 获取单个歌曲详情

```kotlin
val song = MusicSearchUtil.getSongDetail(songId)
println("${song?.name} - ${song?.album?.name}")
```

#### 批量获取歌曲详情

```kotlin
val songs = MusicSearchUtil.getSongDetails(listOf(186016, 186017, 186018))
songs.forEach { song ->
    println("${song.name} - ${song.album.name}")
}
```

### 组合查询

#### 搜索并获取完整信息（含歌词）

```kotlin
val songsWithLyrics = MusicSearchUtil.searchWithLyrics(
    songName = "晴天",
    artistName = "周杰伦"
)

songsWithLyrics.forEach { result ->
    println("歌曲: ${result.song.name}")
    println("歌手: ${result.song.artists.joinToString { it.name }}")
    println("专辑: ${result.song.album.name}")
    if (result.lyric != null) {
        println("歌词:\n${result.lyric.lrc?.lyric}")
    }
}
```

## 数据模型

### Song（歌曲）

```kotlin
data class Song(
    val id: Long,              // 歌曲 ID
    val name: String,          // 歌名
    val artists: List<Artist>, // 歌手列表
    val album: Album,          // 专辑信息
    val duration: Long,        // 时长（毫秒）
    val mvId: Long?,          // MV ID
    val fee: Int?,            // 收费类型
    val privilege: Privilege? // 权限信息
)
```

### Artist（歌手）

```kotlin
data class Artist(
    val id: Long,           // 歌手 ID
    val name: String,       // 歌手名
    val picUrl: String?,    // 头像 URL
    val alias: List<String>?, // 别名
    val albumSize: Int?,    // 专辑数
    val musicSize: Int?     // 歌曲数
)
```

### Album（专辑）

```kotlin
data class Album(
    val id: Long,           // 专辑 ID
    val name: String,       // 专辑名
    val picUrl: String?,    // 封面 URL
    val artist: Artist?,    // 歌手信息
    val publishTime: Long?, // 发布时间
    val size: Int?          // 歌曲数
)
```

### LyricResponse（歌词）

```kotlin
data class LyricResponse(
    val code: Int,
    val lrc: LyricContent?,      // 原文歌词
    val tlyric: LyricContent?,   // 翻译歌词
    val romalrc: LyricContent?   // 罗马音歌词
)

data class LyricContent(
    val version: Int?,
    val lyric: String?  // 歌词文本（带时间轴）
)
```

### SongWithLyric（歌曲与歌词组合）

```kotlin
data class SongWithLyric(
    val song: Song,           // 歌曲信息
    val lyric: LyricResponse  // 歌词信息
)
```

## 搜索类型

```kotlin
enum class SearchType {
    SONG,      // 单曲
    ALBUM,     // 专辑
    ARTIST,    // 歌手
    PLAYLIST,  // 歌单
    USER,      // 用户
    MV,        // MV
    LYRIC,     // 歌词
    RADIO,     // 电台
    VIDEO      // 视频
}
```

## 完整示例

```kotlin
fun main() {
    // 1. 搜索歌曲
    val songs = MusicSearchUtil.searchSongs("晴天", limit = 5)
    
    // 2. 精确搜索
    val exactSongs = MusicSearchUtil.searchBySongAndArtist("晴天", "周杰伦")
    
    // 3. 获取歌词
    if (exactSongs.isNotEmpty()) {
        val song = exactSongs.first()
        val lyric = MusicSearchUtil.getLyric(song.id)
        
        println("歌曲: ${song.name}")
        println("歌手: ${song.artists.joinToString { it.name }}")
        println("专辑: ${song.album.name}")
        println("\n歌词:")
        println(lyric.lrc?.lyric)
    }
    
    // 4. 根据歌词搜索
    val songsByLyric = MusicSearchUtil.searchByLyric("刮风这天我试过握着你手")
    songsByLyric.forEach { song ->
        println("${song.name} - ${song.artists.joinToString { it.name }}")
    }
    
    // 5. 搜索歌手
    val artists = MusicSearchUtil.searchArtists("周杰伦")
    artists.forEach { artist ->
        println("${artist.name} - 专辑数: ${artist.albumSize}")
    }
    
    // 6. 搜索专辑
    val albums = MusicSearchUtil.searchAlbums("叶惠美")
    albums.forEach { album ->
        println("${album.name} - ${album.artist?.name}")
    }
    
    // 7. 一键搜索（含歌词）
    val withLyrics = MusicSearchUtil.searchWithLyrics("稻香", "周杰伦")
    withLyrics.forEach { result ->
        println("${result.song.name}")
        println(result.lyric.lrc?.lyric?.take(200))
    }
    
    // 8. 根据歌词片段获取完整歌词
    val lyricResults = MusicSearchUtil.getLyricsByFragment("刮风这天我试过握着你手", limit = 3)
    lyricResults.forEach { result ->
        println("${result.song.name} - ${result.song.artists.joinToString { it.name }}")
    }
}
```

## 注意事项

1. **无需 Token**: 本模块使用公开 API，无需申请 Token
2. **请求频率**: 建议控制请求频率，避免被限流
3. **数据来源**: 基于网易云音乐公开 API
4. **歌词格式**: 歌词包含时间轴信息，格式如 `[00:00.00]歌词内容`
5. **版权限制**: 部分歌曲可能因版权原因无法获取完整信息

## 使用场景

- 🎵 音乐播放器开发
- 📝 歌词展示应用
- 🔍 音乐搜索引擎
- 📊 音乐数据分析
- 🎤 K歌应用
- 📱 音乐推荐系统

## 依赖

```kotlin
dependencies {
    implementation("com.squareup.okhttp3:okhttp:4.x.x")
    implementation("com.alibaba.fastjson2:fastjson2-kotlin:2.x.x")
}
```

## API 来源

本模块基于网易云音乐公开 API，仅供学习和个人使用。
