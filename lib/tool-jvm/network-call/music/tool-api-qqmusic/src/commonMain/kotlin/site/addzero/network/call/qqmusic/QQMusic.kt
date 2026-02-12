package site.addzero.network.call.qqmusic

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import site.addzero.network.call.qqmusic.model.*

/**
 * QQ 音乐便捷门面
 *
 * 封装请求体构建逻辑，简化调用方式
 *
 * @param mainApi [QQMusicMainApi] 实例 (baseUrl = "https://u.y.qq.com/")
 * @param qzoneApi [QQMusicQzoneApi] 实例 (baseUrl = "https://i.y.qq.com/")
 */
class QQMusic(
    private val mainApi: QQMusicMainApi,
    private val qzoneApi: QQMusicQzoneApi
) {

    /**
     * 获取歌曲播放 URL
     *
     * @param songmid 歌曲 MID
     * @param quality 音质: "m4a", "128", "320"(默认)
     * @return 完整播放 URL，失败返回 null
     */
    suspend fun getMusicUrl(songmid: String, quality: String = "320"): String? {
        val q = MusicQuality.fromString(quality)
        val filename = "${q.prefix}${songmid}${songmid}.${q.suffix}"
        val request = GetVkeyRequest(
            req1 = VkeyReq1(param = VkeyParam(filename = listOf(filename), songmid = listOf(songmid)))
        )
        val resp = mainApi.getVkey(request)
        val sip = resp.req1?.data?.sip?.firstOrNull() ?: return null
        val purl = resp.req1.data.midurlinfo.firstOrNull()?.purl ?: return null
        return if (purl.isNotEmpty()) sip + purl else null
    }

    /**
     * 关键词搜索
     */
    suspend fun search(
        keyword: String,
        searchType: SearchType = SearchType.SONG,
        resultNum: Int = 50,
        pageNum: Int = 1
    ): JsonElement? {
        val request = SearchRequest(
            req = SearchReq(
                param = SearchParam(
                    query = keyword,
                    searchType = searchType.value,
                    numPerPage = resultNum,
                    pageNum = pageNum
                )
            )
        )
        val resp = mainApi.search(request)
        val body = resp.req?.data?.body ?: return null
        return when (searchType) {
            SearchType.SONG, SearchType.LYRIC -> body.song
            SearchType.ALBUM -> body.album
            SearchType.PLAYLIST -> body.songlist
            SearchType.MV -> body.mv
            SearchType.USER -> body.user
        }
    }

    /**
     * 获取歌单歌曲列表
     */
    suspend fun getSongList(categoryId: String): List<JsonObject> {
        return qzoneApi.getSongList(categoryId = categoryId).cdlist.firstOrNull()?.songlist ?: emptyList()
    }

    /**
     * 获取歌单名称
     */
    suspend fun getSongListName(categoryId: String): String? {
        return qzoneApi.getSongList(categoryId = categoryId).cdlist.firstOrNull()?.dissname
    }

    /**
     * 获取歌词（原始文本）
     */
    suspend fun getLyric(songmid: String): String {
        val resp = qzoneApi.getLyric(songmid = songmid)
        return resp.lyric + "\n" + resp.trans
    }

    /**
     * 获取歌词（解析后）
     */
    suspend fun getParsedLyric(songmid: String): ParsedLyric {
        val resp = qzoneApi.getLyric(songmid = songmid)
        return LyricParser.parse(resp)
    }

    /**
     * 获取专辑歌曲列表
     */
    suspend fun getAlbumSongList(albummid: String): List<JsonObject> {
        return qzoneApi.getAlbumSongList(albummid = albummid).data?.list ?: emptyList()
    }

    /**
     * 获取专辑名称
     */
    suspend fun getAlbumName(albummid: String): String? {
        return qzoneApi.getAlbumSongList(albummid = albummid).data?.name
    }

    /**
     * 获取 MV 信息
     */
    suspend fun getMVInfo(vid: String): JsonObject {
        val request = MVRequest(
            mvInfo = MVInfoReq(param = MVInfoParam(vidlist = listOf(vid))),
            mvUrl = MVUrlReq(param = MVUrlParam(vids = listOf(vid)))
        )
        return mainApi.getMVInfo(request)
    }

    /**
     * 获取歌手信息
     */
    suspend fun getSingerInfo(singermid: String): JsonObject? {
        val dataParam = """{"comm":{"ct":24,"cv":0},"singer":{"method":"get_singer_detail_info","param":{"sort":5,"singermid":"$singermid","sin":0,"num":50},"module":"music.web_singer_info_svr"}}"""
        return mainApi.getSingerInfo(data = dataParam).singer?.data
    }

    /**
     * 获取专辑封面图 URL
     */
    fun getAlbumCoverImage(albummid: String): String {
        return "https://y.gtimg.cn/music/photo_new/T002R300x300M000${albummid}.jpg"
    }
}
