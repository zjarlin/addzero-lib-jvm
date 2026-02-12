package site.addzero.network.call.qqmusic

import de.jensklingenberg.ktorfit.http.*
import site.addzero.network.call.qqmusic.model.*

/**
 * QQ 音乐 Qzone 接口 (i.y.qq.com)
 *
 * 歌单、歌词相关
 * baseUrl = "https://i.y.qq.com/"
 */
interface QQMusicQzoneApi {

    /**
     * 获取歌单歌曲信息
     */
    @GET("qzone-music/fcg-bin/fcg_ucc_getcdinfo_byids_cp.fcg")
    suspend fun getSongList(
        @Query("disstid") categoryId: String,
        @Query("type") type: Int = 1,
        @Query("json") json: Int = 1,
        @Query("utf8") utf8: Int = 1,
        @Query("onlysong") onlysong: Int = 0,
        @Query("nosign") nosign: Int = 1,
        @Query("g_tk") gTk: Int = 5381,
        @Query("loginUin") loginUin: Int = 0,
        @Query("hostUin") hostUin: Int = 0,
        @Query("format") format: String = "json",
        @Query("inCharset") inCharset: String = "GB2312",
        @Query("outCharset") outCharset: String = "utf-8",
        @Query("notice") notice: Int = 0,
        @Query("platform") platform: String = "yqq",
        @Query("needNewCode") needNewCode: Int = 0
    ): SongListResponse

    /**
     * 获取歌曲歌词
     */
    @GET("lyric/fcgi-bin/fcg_query_lyric_new.fcg")
    suspend fun getLyric(
        @Query("songmid") songmid: String,
        @Query("g_tk") gTk: Int = 5381,
        @Query("format") format: String = "json",
        @Query("inCharset") inCharset: String = "utf8",
        @Query("outCharset") outCharset: String = "utf-8",
        @Query("nobase64") nobase64: Int = 1
    ): LyricResponse

    /**
     * 获取专辑歌曲信息
     */
    @GET("v8/fcg-bin/fcg_v8_album_info_cp.fcg")
    suspend fun getAlbumSongList(
        @Query("albummid") albummid: String,
        @Query("platform") platform: String = "h5page",
        @Query("g_tk") gTk: Int = 938407465,
        @Query("uin") uin: Int = 0,
        @Query("format") format: String = "json",
        @Query("inCharset") inCharset: String = "utf-8",
        @Query("outCharset") outCharset: String = "utf-8",
        @Query("notice") notice: Int = 0,
        @Query("needNewCode") needNewCode: Int = 1
    ): AlbumResponse
}
