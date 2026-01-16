package site.addzero.network.call.music.model

/**
 * 音乐搜索类型
 */
enum class SearchType(val value: Int) {
    /** 单曲 */
    SONG(1),
    /** 专辑 */
    ALBUM(10),
    /** 歌手 */
    ARTIST(100),
    /** 歌单 */
    PLAYLIST(1000),
    /** 用户 */
    USER(1002),
    /** MV */
    MV(1004),
    /** 歌词 */
    LYRIC(1006),
    /** 电台 */
    RADIO(1009),
    /** 视频 */
    VIDEO(1014)
}

/**
 * 音乐搜索请求
 */
data class MusicSearchRequest(
    /** 搜索关键词 */
    val keywords: String,
    /** 搜索类型 */
    val type: SearchType = SearchType.SONG,
    /** 每页数量 */
    val limit: Int = 30,
    /** 偏移量 */
    val offset: Int = 0
)
