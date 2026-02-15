package site.addzero.vibepocket.api.music

import kotlinx.serialization.Serializable

/**
 * 统一歌曲模型
 */
@Serializable
data class MusicTrack(
    /** 平台内唯一 ID（字符串统一，QQ 用 mid，网易用数字 id 转字符串） */
    val id: String,
    /** 歌名 */
    val name: String,
    /** 歌手名（多个用 ", " 拼接） */
    val artist: String = "",
    /** 专辑名 */
    val album: String = "",
    /** 封面图 URL */
    val coverUrl: String? = null,
    /** 时长（毫秒） */
    val durationMs: Long = 0,
    /** 来源平台标识 */
    val platform: String = "",
)

/**
 * 统一歌词模型
 */
@Serializable
data class MusicLyric(
    /** 原始 LRC 歌词文本 */
    val lrc: String,
    /** 翻译歌词（可能为空） */
    val translatedLrc: String? = null,
)
