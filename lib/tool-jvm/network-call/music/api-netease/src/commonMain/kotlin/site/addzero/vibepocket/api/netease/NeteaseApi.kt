package site.addzero.vibepocket.api.netease

import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.Query


/**
 * 网易云音乐 API 接口定义（Ktorfit 声明式）
 *
 * 纯 HTTP 接口声明，不含业务逻辑。
 * type: 1=歌曲, 10=专辑, 100=歌手, 1000=歌单, 1006=歌词
 */
interface NeteaseApi {

    /** 通用搜索（按 type 区分搜索类型） */
    @GET("search/get/web")
    suspend fun search(
        @Query s: String,
        @Query type: Int = SearchType.SONG.value,
        @Query limit: Int = 30,
        @Query offset: Int = 0,
//        @Query("s") s: String,
//        @Query("type") type: Int = 1,
//        @Query("limit") limit: Int = 30,
//        @Query("offset") offset: Int = 0,
//        @Header("User-Agent") userAgent: String = UA,
//        @Header("Referer") referer: String = REFERER,
    ): NeteaseSearchResponse

    /** 获取歌词 */
    @GET("song/lyric")
    suspend fun getLyric(
        @Query id: Long,
        @Query lv: Int = 1,
        @Query tv: Int = 1,
    ): NeteaseLyricResponse

    /** 获取歌曲详情 */
    @GET("song/detail")
    suspend fun getSongDetail(
        @Query ids: String,
    ): SongDetailResponse

    companion object {
        const val UA = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36"
        const val REFERER = "https://music.163.com/"
    }
}
