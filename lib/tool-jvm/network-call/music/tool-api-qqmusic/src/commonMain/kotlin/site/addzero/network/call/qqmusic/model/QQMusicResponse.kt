package site.addzero.network.call.qqmusic.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

// ============ 获取音乐 URL 响应 ============

@Serializable
data class VkeyResponse(
    @SerialName("req_1") val req1: VkeyReq1Data? = null
)

@Serializable
data class VkeyReq1Data(
    val data: VkeyData? = null
)

@Serializable
data class VkeyData(
    val sip: List<String> = emptyList(),
    val midurlinfo: List<MidUrlInfo> = emptyList()
)

@Serializable
data class MidUrlInfo(
    val purl: String = ""
)

// ============ 搜索响应 ============

@Serializable
data class SearchResponse(
    val req: SearchReqData? = null
)

@Serializable
data class SearchReqData(
    val data: SearchBodyWrapper? = null
)

@Serializable
data class SearchBodyWrapper(
    val body: SearchBody? = null
)

@Serializable
data class SearchBody(
    val song: JsonElement? = null,
    val album: JsonElement? = null,
    val songlist: JsonElement? = null,
    val mv: JsonElement? = null,
    val user: JsonElement? = null
)


// ============ 歌单响应 ============

@Serializable
data class SongListResponse(
    val cdlist: List<CdInfo> = emptyList()
)

@Serializable
data class CdInfo(
    val dissname: String? = null,
    val songlist: List<JsonObject> = emptyList()
)

// ============ 歌词响应 ============

@Serializable
data class LyricResponse(
    val lyric: String = "",
    val trans: String = ""
)

// ============ 专辑响应 ============

@Serializable
data class AlbumResponse(
    val data: AlbumData? = null
)

@Serializable
data class AlbumData(
    val name: String? = null,
    val list: List<JsonObject> = emptyList()
)

// ============ 歌手响应 ============

@Serializable
data class SingerResponse(
    val singer: SingerWrapper? = null
)

@Serializable
data class SingerWrapper(
    val data: JsonObject? = null
)
