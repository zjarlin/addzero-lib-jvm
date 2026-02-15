package site.addzero.vibepocket.api.music

/**
 * 音乐平台统一 SPI 接口
 *
 * 各平台（网易云、QQ音乐等）实现此接口，上层业务只依赖此抽象。
 */
interface MusicService {

    /** 平台标识，如 "netease"、"qqmusic" */
    val platformId: String

    /** 关键字搜歌 */
    suspend fun searchSongs(
        keyword: String,
        limit: Int = 10,
        offset: Int = 0,
    ): List<MusicTrack>

    /** 根据平台音乐 ID 查详情 */
    suspend fun getTrackDetail(trackId: String): MusicTrack?

    /** 获取歌词（原始 LRC 文本） */
    suspend fun getLyric(trackId: String): MusicLyric?
}
