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
) {
    val artistNames get() = artists.joinToString(", ") { it.name }
    /** 封面图 URL（优先取专辑封面） */
    val coverUrl: String? get() = album?.picUrl
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
