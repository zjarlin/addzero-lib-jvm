package site.addzero.network.call.music.model

import com.alibaba.fastjson2.annotation.JSONField

/**
 * 搜索响应
 */
data class MusicSearchResponse(
    val code: Int,
    val result: SearchResult?
)

/**
 * 搜索结果
 */
data class SearchResult(
    val songs: List<Song>?,
    val songCount: Int?,
    val albums: List<Album>?,
    val albumCount: Int?,
    val artists: List<Artist>?,
    val artistCount: Int?,
    val playlists: List<Playlist>?,
    val playlistCount: Int?
)

/**
 * 歌曲信息
 */
data class Song(
    val id: Long,
    val name: String,
    val artists: List<Artist>,
    val album: Album,
    val duration: Long,
    @JSONField(name = "mvid")
    val mvId: Long?,
    val fee: Int?,
    val privilege: Privilege?
)

/**
 * 艺术家信息
 */
data class Artist(
    val id: Long,
    val name: String,
    @JSONField(name = "picUrl")
    val picUrl: String?,
    val alias: List<String>?,
    val albumSize: Int?,
    val musicSize: Int?
)

/**
 * 专辑信息
 */
data class Album(
    val id: Long,
    val name: String,
    @JSONField(name = "picUrl")
    val picUrl: String?,
    val artist: Artist?,
    val publishTime: Long?,
    val size: Int?
)

/**
 * 歌单信息
 */
data class Playlist(
    val id: Long,
    val name: String,
    @JSONField(name = "coverImgUrl")
    val coverImgUrl: String?,
    val creator: Creator?,
    val trackCount: Int?,
    val playCount: Long?,
    val description: String?
)

/**
 * 创建者信息
 */
data class Creator(
    @JSONField(name = "userId")
    val userId: Long,
    val nickname: String,
    @JSONField(name = "avatarUrl")
    val avatarUrl: String?
)

/**
 * 权限信息
 */
data class Privilege(
    val id: Long,
    val fee: Int,
    val st: Int,
    val pl: Int,
    val dl: Int,
    val maxbr: Int
)

/**
 * 歌词响应
 */
data class LyricResponse(
    val code: Int,
    val lrc: LyricContent?,
    val tlyric: LyricContent?,
    val romalrc: LyricContent?
)

/**
 * 歌词内容
 */
data class LyricContent(
    val version: Int?,
    val lyric: String?
)

/**
 * 歌曲详情响应
 */
data class SongDetailResponse(
    val code: Int,
    val songs: List<Song>?,
    val privileges: List<Privilege>?
)

/**
 * 歌曲 URL 响应
 */
data class SongUrlResponse(
    val code: Int,
    val data: List<SongUrl>?
)

/**
 * 歌曲 URL 信息
 */
data class SongUrl(
    val id: Long,
    val url: String?,
    val br: Int?,
    val size: Long?,
    val md5: String?,
    val type: String?,
    val level: String?
)

/**
 * 歌曲与歌词组合
 * 用于返回搜索结果时同时包含歌曲信息和歌词
 */
data class SongWithLyric(
    /** 歌曲信息 */
    val song: Song,
    /** 歌词信息 */
    val lyric: LyricResponse
)
