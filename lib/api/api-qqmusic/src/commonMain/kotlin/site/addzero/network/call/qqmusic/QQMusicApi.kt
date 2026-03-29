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
        @Header("Accept") accept: String = "application/json, text/plain, */*",
        @Header("Accept-Language") acceptLang: String = "zh-CN,zh;q=0.9,en;q=0.8",
        @Header("Content-Type") contentType: String = "application/json;charset=UTF-8",
        @Header("Referer") referer: String = "https://y.qq.com/",
        @Header("Sec-Fetch-Dest") secDest: String = "empty",
        @Header("Sec-Fetch-Mode") secMode: String = "cors",
        @Header("Sec-Fetch-Site") secSite: String = "none",
    ): VkeyResponse

    /**
     * 关键词搜索
     */
    @POST("cgi-bin/musicu.fcg")
    suspend fun search(
        @Body body: SearchRequest,
        @Header("User-Agent") userAgent: String = "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:109.0) Gecko/20100101 Firefox/115.0",
        @Header("Accept") accept: String = "application/json, text/plain, */*",
        @Header("Accept-Language") acceptLang: String = "zh-CN,zh;q=0.8,zh-TW;q=0.7,zh-HK;q=0.5,en-US;q=0.3,en;q=0.2",
        @Header("Content-Type") contentType: String = "application/json;charset=utf-8",
        @Header("Sec-Fetch-Dest") secDest: String = "empty",
        @Header("Sec-Fetch-Mode") secMode: String = "cors",
        @Header("Sec-Fetch-Site") secSite: String = "same-origin",
    ): SearchResponse

    /**
     * 获取 MV 信息及播放地址
     */
    @POST("cgi-bin/musicu.fcg")
    suspend fun getMVInfo(
        @Body body: MVRequest,
        @Header("User-Agent") userAgent: String = "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:109.0) Gecko/20100101 Firefox/115.0",
        @Header("Accept") accept: String = "*/*",
        @Header("Accept-Language") acceptLang: String = "zh-CN,zh;q=0.8,zh-TW;q=0.7,zh-HK;q=0.5,en-US;q=0.3,en;q=0.2",
        @Header("Content-type") contentTypeHeader: String = "application/x-www-form-urlencoded",
        @Header("Referer") referer: String = "https://y.qq.com/",
        @Header("Sec-Fetch-Dest") secDest: String = "empty",
        @Header("Sec-Fetch-Mode") secMode: String = "cors",
        @Header("Sec-Fetch-Site") secSite: String = "same-site",
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
