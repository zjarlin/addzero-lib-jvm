package site.addzero.network.call.qqmusic

import de.jensklingenberg.ktorfit.http.*
import kotlinx.serialization.json.JsonObject
import site.addzero.network.call.qqmusic.model.*

/**
 * QQ 音乐主接口 (u.y.qq.com)
 *
 * 基于 [copws/qq-music-api](https://github.com/copws/qq-music-api) 改造
 * baseUrl = "https://u.y.qq.com/"
 */
interface QQMusicMainApi {

    /**
     * 获取歌曲播放 URL
     */
    @POST("cgi-bin/musicu.fcg")
    suspend fun getVkey(
        @Body body: GetVkeyRequest,
        @Header("Referer") referer: String = "https://y.qq.com/"
    ): VkeyResponse

    /**
     * 关键词搜索
     */
    @POST("cgi-bin/musicu.fcg")
    suspend fun search(
        @Body body: SearchRequest
    ): SearchResponse

    /**
     * 获取 MV 信息及播放地址
     */
    @POST("cgi-bin/musicu.fcg")
    suspend fun getMVInfo(
        @Body body: MVRequest,
        @Header("Referer") referer: String = "https://y.qq.com/"
    ): JsonObject

    /**
     * 获取歌手信息
     */
    @GET("cgi-bin/musicu.fcg")
    suspend fun getSingerInfo(
        @Query("format") format: String = "json",
        @Query("loginUin") loginUin: Int = 0,
        @Query("hostUin") hostUin: Int = 0,
        @Query("inCharset") inCharset: String = "utf8",
        @Query("outCharset") outCharset: String = "utf-8",
        @Query("platform") platform: String = "yqq.json",
        @Query("needNewCode") needNewCode: Int = 0,
        @Query("data") data: String
    ): SingerResponse
}
