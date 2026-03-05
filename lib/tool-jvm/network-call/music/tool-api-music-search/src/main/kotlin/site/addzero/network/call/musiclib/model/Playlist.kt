package site.addzero.network.call.musiclib.model

import kotlinx.serialization.Serializable

/**
 * 歌单数据模型 - 所有音乐源通用的歌单结构
 */
@Serializable
data class Playlist(
    val id: String = "",
    val name: String = "",
    val cover: String = "",
    val trackCount: Long = 0,
    val playCount: Long = 0,
    val creator: String = "",
    val description: String = "",
    val source: String = "",
    val link: String = "",
    val extra: Map<String, String> = emptyMap()  // 源特有的元数据
)

/**
 * 歌单详情（包含歌曲列表）
 */
data class PlaylistDetail(
    val playlist: Playlist,
    val songs: List<Song>
)
