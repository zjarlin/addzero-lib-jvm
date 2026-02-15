package site.addzero.vibepocket.api.netease.model

import kotlinx.serialization.Serializable

@Serializable
data class NeteaseSearchRes(
  val code: Int = 0,
  val result: NeteaseSearchResult? = null,
)

enum class MusicSearchType(val value: Int) {
    SONG(1),
    ALBUM(10),
    ARTIST(100),
    PLAYLIST(1000),
    LYRIC(1006),
}

@Serializable
data class NeteaseSearchResult(
  val songs: List<NeteaseSearchSong>? = null,
  val songCount: Int? = null,
  /**
     * 歌手
     */
    val artists: List<NeteaseArtist>? = null,
  val artistCount: Int? = null,
  /**
     * 专辑
     */
    val albums: List<NeteaseAlbum>? = null,
  val albumCount: Int? = null,

  /**
     * 播放列表
     */
    val playlists: List<NeteasePlaylist>? = null,
  val playlistCount: Int? = null,
)

@Serializable
data class NeteasePlaylist(
    val id: Long = 0,
    val name: String = "",
    val coverImgUrl: String? = null,
    val trackCount: Int = 0,
    val playCount: Long = 0,
    val description: String? = null,
)

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

data class SongWithLyric(
  val song: NeteaseSearchSong,
  val lyric: NeteaseLyricResponse,
)
