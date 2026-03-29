package site.addzero.network.call.qqmusic.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ============ 获取音乐 URL 请求 ============

@Serializable
data class GetVkeyRequest(
    @SerialName("req_1") val req1: VkeyReq1,
    val loginUin: String = "0",
    val comm: CommParam = CommParam()
)

@Serializable
data class VkeyReq1(
    val module: String = "vkey.GetVkeyServer",
    val method: String = "CgiGetVkey",
    val param: VkeyParam
)

@Serializable
data class VkeyParam(
    val filename: List<String>,
    val guid: String = "10000",
    val songmid: List<String>,
    val songtype: List<Int> = listOf(0),
    val uin: String = "0",
    val loginflag: Int = 1,
    val platform: String = "20"
)

@Serializable
data class CommParam(
    val uin: String = "0",
    val format: String = "json",
    val ct: Int = 24,
    val cv: Int = 0
)

// ============ 搜索请求 ============

@Serializable
data class SearchRequest(
    val comm: SearchComm = SearchComm(),
    val req: SearchReq
)

@Serializable
data class SearchComm(
    val ct: String = "19",
    val cv: String = "1859",
    val uin: String = "0"
)


@Serializable
data class SearchReq(
    val method: String = "DoSearchForQQMusicDesktop",
    val module: String = "music.search.SearchCgiService",
    val param: SearchParam
)

@Serializable
data class SearchParam(
    val grp: Int = 1,
    @SerialName("num_per_page") val numPerPage: Int = 50,
    @SerialName("page_num") val pageNum: Int = 1,
    val query: String,
    @SerialName("search_type") val searchType: Int = 0
)

// ============ MV 请求 ============

@Serializable
data class MVRequest(
    val comm: MVComm = MVComm(),
    val mvInfo: MVInfoReq,
    val mvUrl: MVUrlReq
)

@Serializable
data class MVComm(
    val ct: Int = 6,
    val cv: Int = 0,
    @SerialName("g_tk") val gTk: Long = 1646675364,
    val uin: Int = 0,
    val format: String = "json",
    val platform: String = "yqq"
)

@Serializable
data class MVInfoReq(
    val module: String = "music.video.VideoData",
    val method: String = "get_video_info_batch",
    val param: MVInfoParam
)

@Serializable
data class MVInfoParam(
    val vidlist: List<String>,
    val required: List<String> = listOf(
        "vid", "type", "sid", "cover_pic", "duration", "singers",
        "new_switch_str", "video_pay", "hint", "code", "msg", "name",
        "desc", "playcnt", "pubdate", "isfav", "fileid", "filesize_v2",
        "switch_pay_type", "pay", "pay_info", "uploader_headurl",
        "uploader_nick", "uploader_uin", "uploader_encuin", "play_forbid_reason"
    )
)

@Serializable
data class MVUrlReq(
    val module: String = "music.stream.MvUrlProxy",
    val method: String = "GetMvUrls",
    val param: MVUrlParam
)

@Serializable
data class MVUrlParam(
    val vids: List<String>,
    @SerialName("request_type") val requestType: Int = 10003,
    val addrtype: Int = 3,
    val format: Int = 264,
    val maxFiletype: Int = 60
)
