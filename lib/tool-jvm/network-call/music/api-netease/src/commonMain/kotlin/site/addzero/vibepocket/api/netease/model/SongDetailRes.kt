package site.addzero.vibepocket.api.netease.model

import kotlinx.serialization.Serializable

@Serializable
data class SongDetailResponse(
    val code: Int = 0,
    val songs: List<NeteaseSearchSong>? = null,
)

@Serializable
data class NeteaseSearchSong(
    val id: Long,
    val name: String,
    val artists: List<NeteaseArtist> = emptyList(),
    val album: NeteaseAlbum? = null,
    val duration: Long = 0,
    /** 匹配到的歌词片段（API 返回 List<String>，每项可能含 HTML 高亮标签） */
    val lyrics: List<String>? = null,
) {
    val artistNames get() = artists.joinToString(", ") { it.name }
    /** 封面图 URL（优先取专辑封面） */
    val coverUrl: String? get() = album?.picUrl
    /** 提取匹配的歌词文本（去掉 HTML 高亮标签，合并为一行） */
    val matchedLyricText: String?
        get() = lyrics?.joinToString(" ") { it.replace(Regex("<[^>]+>"), "") }
}

@Serializable
data class NeteaseArtist(
    val id: Long = 0,
    val name: String = "",
    val picUrl: String? = null,
)

@Serializable
data class NeteaseAlbum(
    val id: Long = 0,
    val name: String = "",
    val picUrl: String? = null,
)
