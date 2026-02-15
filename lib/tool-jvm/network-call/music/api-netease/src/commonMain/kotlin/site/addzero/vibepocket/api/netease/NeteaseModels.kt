package site.addzero.vibepocket.api.netease

import kotlinx.serialization.Serializable

// ── 搜索类型枚举 ────────────────────────────────────────────

enum class SearchType(val value: Int) {
    SONG(1),
    ALBUM(10),
    ARTIST(100),
    PLAYLIST(1000),
    LYRIC(1006),
}

// ── 搜索请求 ────────────────────────────────────────────────

data class MusicSearchRequest(
    val keywords: String,
    val type: SearchType = SearchType.SONG,
    val limit: Int = 30,
    val offset: Int = 0,
)

// ── 通用搜索响应（type=1/10/100/1000） ──────────────────────

@Serializable
data class NeteaseSearchResponse(
    val code: Int = 0,
    val result: NeteaseSearchResult? = null,
)

@Serializable
data class NeteaseSearchResult(
    val songs: List<NeteaseSearchSong>? = null,
    val songCount: Int? = null,
    val artists: List<NeteaseArtist>? = null,
    val artistCount: Int? = null,
    val albums: List<NeteaseAlbum>? = null,
    val albumCount: Int? = null,
    val playlists: List<NeteasePlaylist>? = null,
    val playlistCount: Int? = null,
)

// ── 歌曲 ────────────────────────────────────────────────────

@Serializable
data class NeteaseSearchSong(
    val id: Long,
    val name: String,
    val artists: List<NeteaseArtist> = emptyList(),
    val album: NeteaseAlbum? = null,
    val duration: Long = 0,
) {
    val artistNames get() = artists.joinToString(", ") { it.name }
    /** 封面图 URL（优先取专辑封面） */
    val coverUrl: String? get() = album?.picUrl
}

// ── 歌手 ────────────────────────────────────────────────────

@Serializable
data class NeteaseArtist(
    val id: Long = 0,
    val name: String = "",
    val picUrl: String? = null,
)

// ── 专辑 ────────────────────────────────────────────────────

@Serializable
data class NeteaseAlbum(
    val id: Long = 0,
    val name: String = "",
    val picUrl: String? = null,
)

// ── 歌单 ────────────────────────────────────────────────────

@Serializable
data class NeteasePlaylist(
    val id: Long = 0,
    val name: String = "",
    val coverImgUrl: String? = null,
    val trackCount: Int = 0,
    val playCount: Long = 0,
    val description: String? = null,
)

// ── 歌词搜索（type=1006）响应 ───────────────────────────────

@Serializable
data class NeteaseLyricSearchResponse(
    val code: Int = 0,
    val result: NeteaseLyricSearchResult? = null,
)

@Serializable
data class NeteaseLyricSearchResult(
    val songs: List<NeteaseLyricSearchSong>? = null,
    val songCount: Int? = null,
)

@Serializable
data class NeteaseLyricSearchSong(
    val id: Long,
    val name: String,
    val artists: List<NeteaseArtist> = emptyList(),
    val album: NeteaseAlbum? = null,
    val duration: Long = 0,
    /** 匹配到的歌词片段（API 返回 List<String>，每项可能含 HTML 高亮标签） */
    val lyrics: List<String>? = null,
) {
    val artistNames get() = artists.joinToString(", ") { it.name }
    val coverUrl: String? get() = album?.picUrl
    /** 提取匹配的歌词文本（去掉 HTML 高亮标签，合并为一行） */
    val matchedLyricText: String?
        get() = lyrics?.joinToString(" ") { it.replace(Regex("<[^>]+>"), "") }
}

// ── 歌词详情响应 ────────────────────────────────────────────

@Serializable
data class NeteaseLyricResponse(
    val code: Int = 0,
    val lrc: NeteaseLrc? = null,
    val tlyric: NeteaseLrc? = null,
)

@Serializable
data class NeteaseLrc(
    val lyric: String? = null,
)

// ── 歌曲详情响应 ────────────────────────────────────────────

@Serializable
data class SongDetailResponse(
    val code: Int = 0,
    val songs: List<NeteaseSearchSong>? = null,
)

// ── 歌曲+歌词组合（业务层使用） ─────────────────────────────

data class SongWithLyric(
    val song: NeteaseSearchSong,
    val lyric: NeteaseLyricResponse,
)
