package site.addzero.network.call.musiclib.api.netease

import site.addzero.network.call.musiclib.crypto.NeteaseCrypto
import site.addzero.network.call.musiclib.model.Playlist
import site.addzero.network.call.musiclib.model.PlaylistDetail
import site.addzero.network.call.musiclib.model.Song
import site.addzero.network.call.musiclib.provider.CookieMusicProvider
import site.addzero.network.call.musiclib.utils.HttpClientManager
import site.addzero.network.call.musiclib.utils.defaultHeaders
import io.ktor.client.HttpClient
import io.ktor.client.call.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*

/**
 * 网易云音乐 API 实现
 */
class NeteaseAPI(
    override var cookie: String = "",
    private val client: HttpClient = HttpClientManager.client,
) : CookieMusicProvider {

    private var isVipCache: Boolean? = null

    override val name: String = "网易云音乐"
    override val source: String = "netease"

    companion object {
        const val REFERER = "http://music.163.com/"
        const val SEARCH_API = "http://music.163.com/api/linux/forward"
        const val DOWNLOAD_API = "http://music.163.com/weapi/song/enhance/player/url"
        const val DOWNLOAD_EAPI = "https://interface3.music.163.com/eapi/song/enhance/player/url/v1"
        const val DETAIL_API = "https://music.163.com/weapi/v3/song/detail"
        const val PLAYLIST_API = "https://music.163.com/weapi/v3/playlist/detail"
        const val USER_ACCOUNT_API = "https://music.163.com/weapi/nuser/account/get"
        const val RECOMMENDED_PLAYLIST_API = "https://music.163.com/weapi/personalized/playlist"
        const val LYRIC_API = "https://music.163.com/weapi/song/lyric"
    }

    override suspend fun isVipAccount(): Boolean {
        isVipCache?.let { return it }
        if (cookie.isEmpty()) {
            isVipCache = false
            return false
        }

        val reqData = buildJsonObject { put("csrf_token", "") }
        val (params, encSecKey) = NeteaseCrypto.encryptWeApi(reqData.toString())

        val response = client.submitForm(
            url = USER_ACCOUNT_API,
            formParameters = parameters {
                append("params", params)
                append("encSecKey", encSecKey)
            }
        ) {
            defaultHeaders(REFERER, cookie)
            contentType(ContentType.Application.FormUrlEncoded)
        }

        val json = response.body<JsonObject>()
        val code = json["code"]?.jsonPrimitive?.long ?: -1
        val vipType = json["profile"]?.jsonObject?.get("vipType")?.jsonPrimitive?.long ?: 0

        isVipCache = (code == 200.toLong() && vipType != 0.toLong())
        return isVipCache!!
    }

    override suspend fun search(keyword: String): List<Song> {
        val eparams = buildJsonObject {
            put("method", "POST")
            put("url", "http://music.163.com/api/cloudsearch/pc")
            putJsonObject("params") {
                put("s", keyword)
                put("type", 1)
                put("offset", 0)
                put("limit", 10)
            }
        }

        val encryptedParam = NeteaseCrypto.encryptLinux(eparams.toString())

        val response = client.submitForm(
            url = SEARCH_API,
            formParameters = parameters {
                append("eparams", encryptedParam)
            }
        ) {
            defaultHeaders(REFERER, cookie)
        }

        val json = response.body<JsonObject>()
        val songs = mutableListOf<Song>()
        val isVip = isVipAccount()

        json["result"]?.jsonObject?.get("songs")?.jsonArray?.forEach { songEl ->
            val song = songEl.jsonObject
            val privilege = song["privilege"]?.jsonObject

            // 过滤无版权或收费歌曲
            if (!isVip && (privilege?.get("fl")?.jsonPrimitive?.long ?: 0) == 0.toLong()) {
                return@forEach
            }

            val id = song["id"]?.jsonPrimitive?.long ?: return@forEach
            val name = song["name"]?.jsonPrimitive?.content ?: ""
            val artists = song["ar"]?.jsonArray?.map { it.jsonObject["name"]?.jsonPrimitive?.content ?: "" } ?: emptyList()
            val album = song["al"]?.jsonObject
            val albumName = album?.get("name")?.jsonPrimitive?.content ?: ""
            val picUrl = album?.get("picUrl")?.jsonPrimitive?.content ?: ""
            val duration = (song["dt"]?.jsonPrimitive?.long ?: 0) / 1000

            // 计算文件大小
            var size: Long = 0
            val h = song["h"]?.jsonObject
            val m = song["m"]?.jsonObject
            val l = song["l"]?.jsonObject
            val fl = privilege?.get("fl")?.jsonPrimitive?.long ?: 0

            size = when {
                fl >= 320000 && (h?.get("size")?.jsonPrimitive?.long ?: 0) > 0 -> h?.get("size")?.jsonPrimitive?.long ?: 0
                fl >= 192000 && (m?.get("size")?.jsonPrimitive?.long ?: 0) > 0 -> m?.get("size")?.jsonPrimitive?.long ?: 0
                else -> l?.get("size")?.jsonPrimitive?.long ?: 0
            }

            val bitrate = if (duration > 0 && size > 0) (size * 8 / 1000 / duration).toInt() else 128

            songs.add(Song(
                id = id.toString(),
                name = name,
                artist = artists.joinToString("、"),
                album = albumName,
                duration = duration,
                size = size,
                bitrate = bitrate.toLong(),
                source = "netease",
                cover = picUrl,
                link = "https://music.163.com/#/song?id=$id",
                extra = mapOf("song_id" to id.toString())
            ))
        }

        return songs
    }

    override suspend fun searchPlaylist(keyword: String): List<Playlist> {
        val eparams = buildJsonObject {
            put("method", "POST")
            put("url", "http://music.163.com/api/cloudsearch/pc")
            putJsonObject("params") {
                put("s", keyword)
                put("type", 1000)
                put("offset", 0)
                put("limit", 10)
            }
        }

        val encryptedParam = NeteaseCrypto.encryptLinux(eparams.toString())

        val response = client.submitForm(
            url = SEARCH_API,
            formParameters = parameters {
                append("eparams", encryptedParam)
            }
        ) {
            defaultHeaders(REFERER, cookie)
        }

        val json = response.body<JsonObject>()
        val playlists = mutableListOf<Playlist>()

        json["result"]?.jsonObject?.get("playlists")?.jsonArray?.forEach { plEl ->
            val pl = plEl.jsonObject
            val id = pl["id"]?.jsonPrimitive?.long ?: return@forEach
            val name = pl["name"]?.jsonPrimitive?.content ?: ""
            val cover = pl["coverImgUrl"]?.jsonPrimitive?.content ?: ""
            val creator = pl["creator"]?.jsonObject?.get("nickname")?.jsonPrimitive?.content ?: ""
            val trackCount = pl["trackCount"]?.jsonPrimitive?.long ?: 0
            val playCount = pl["playCount"]?.jsonPrimitive?.long ?: 0
            val description = pl["description"]?.jsonPrimitive?.content ?: ""

            playlists.add(Playlist(
                id = id.toString(),
                name = name,
                cover = cover,
                trackCount = trackCount,
                playCount = playCount,
                creator = creator,
                description = description,
                source = "netease",
                link = "https://music.163.com/#/playlist?id=$id"
            ))
        }

        return playlists
    }

    override suspend fun getPlaylistSongs(id: String): List<Song> {
        val (_, songs) = fetchPlaylistDetail(id)
        return songs
    }

    override suspend fun parsePlaylist(link: String): PlaylistDetail {
        val regex = "playlist\\?id=(\\d+)".toRegex()
        val match = regex.find(link) ?: throw IllegalArgumentException("Invalid netease playlist link")
        val playlistId = match.groupValues[1]
        return fetchPlaylistDetail(playlistId)
    }

    private suspend fun fetchPlaylistDetail(playlistId: String): PlaylistDetail {
        val reqData = buildJsonObject {
            put("id", playlistId)
            put("n", 0)
            put("csrf_token", "")
        }

        val (params, encSecKey) = NeteaseCrypto.encryptWeApi(reqData.toString())

        val response = client.submitForm(
            url = PLAYLIST_API,
            formParameters = parameters {
                append("params", params)
                append("encSecKey", encSecKey)
            }
        ) {
            defaultHeaders(REFERER, cookie)
        }

        val json = response.body<JsonObject>()
        val code = json["code"]?.jsonPrimitive?.long ?: -1
        if (code != 200.toLong()) {
            throw Exception("Netease API error code: $code")
        }

        val playlist = json["playlist"]?.jsonObject ?: throw Exception("Playlist not found")
        val plId = playlist["id"]?.jsonPrimitive?.long ?: 0
        val plName = playlist["name"]?.jsonPrimitive?.content ?: ""
        val plCover = playlist["coverImgUrl"]?.jsonPrimitive?.content ?: ""
        val plDesc = playlist["description"]?.jsonPrimitive?.content ?: ""
        val plPlayCount = playlist["playCount"]?.jsonPrimitive?.long ?: 0
        val plTrackCount = playlist["trackCount"]?.jsonPrimitive?.long ?: 0
        val plCreator = playlist["creator"]?.jsonObject?.get("nickname")?.jsonPrimitive?.content ?: ""

        val playlistObj = Playlist(
            id = plId.toString(),
            name = plName,
            cover = plCover,
            trackCount = plTrackCount,
            playCount = plPlayCount,
            creator = plCreator,
            description = plDesc,
            source = "netease",
            link = "https://music.163.com/#/playlist?id=$plId"
        )

        // 提取所有歌曲ID
        val trackIds = playlist["trackIds"]?.jsonArray?.map { it.jsonObject["id"]?.jsonPrimitive?.long ?: 0 } ?: emptyList()
        val allSongs = mutableListOf<Song>()

        // 分批获取歌曲详情 (每次500首)
        trackIds.chunked(500).forEach { batchIds ->
            val batchSongs = fetchSongsBatch(batchIds.map { it.toString() })
            allSongs.addAll(batchSongs)
        }

        return PlaylistDetail(playlistObj, allSongs)
    }

    private suspend fun fetchSongsBatch(songIds: List<String>): List<Song> {
        if (songIds.isEmpty()) return emptyList()

        val cList = songIds.map { buildJsonObject { put("id", it) } }
        val reqData = buildJsonObject {
            put("c", Json.encodeToString(cList))
            put("ids", Json.encodeToString(songIds))
        }

        val (params, encSecKey) = NeteaseCrypto.encryptWeApi(reqData.toString())

        val response = client.submitForm(
            url = DETAIL_API,
            formParameters = parameters {
                append("params", params)
                append("encSecKey", encSecKey)
            }
        ) {
            defaultHeaders(REFERER, cookie)
        }

        val json = response.body<JsonObject>()
        val songs = mutableListOf<Song>()

        json["songs"]?.jsonArray?.forEach { songEl ->
            val song = songEl.jsonObject
            val id = song["id"]?.jsonPrimitive?.long ?: return@forEach
            val name = song["name"]?.jsonPrimitive?.content ?: ""
            val artists = song["ar"]?.jsonArray?.map { it.jsonObject["name"]?.jsonPrimitive?.content ?: "" } ?: emptyList()
            val album = song["al"]?.jsonObject
            val albumName = album?.get("name")?.jsonPrimitive?.content ?: ""
            val picUrl = album?.get("picUrl")?.jsonPrimitive?.content ?: ""
            val duration = (song["dt"]?.jsonPrimitive?.long ?: 0) / 1000

            songs.add(Song(
                id = id.toString(),
                name = name,
                artist = artists.joinToString("、"),
                album = albumName,
                duration = duration,
                source = "netease",
                cover = picUrl,
                link = "https://music.163.com/#/song?id=$id",
                extra = mapOf("song_id" to id.toString())
            ))
        }

        return songs
    }

    override suspend fun parse(link: String): Song {
        val regex = "id=(\\d+)".toRegex()
        val match = regex.find(link) ?: throw IllegalArgumentException("Invalid netease link")
        val songId = match.groupValues[1]

        val songs = fetchSongsBatch(listOf(songId))
        if (songs.isEmpty()) throw Exception("Song not found")

        val song = songs[0]
        val downloadUrl = getDownloadURL(song)
        return song.copy(url = downloadUrl)
    }

    override suspend fun getDownloadURL(song: Song): String {
        if (song.source != "netease") throw IllegalArgumentException("Source mismatch")

        val songId = song.extra["song_id"] ?: song.id

        // VIP账号尝试获取高音质
        if (isVipAccount()) {
            listOf("hires", "lossless", "exhigh").forEach { quality ->
                try {
                    val url = getEApiDownloadURL(songId, quality)
                    if (url.isNotEmpty()) return url
                } catch (_: Exception) {}
            }
        }

        // 非VIP使用 weapi
        val reqData = buildJsonObject {
            putJsonArray("ids") { add(songId) }
            put("br", 320000)
        }

        val (params, encSecKey) = NeteaseCrypto.encryptWeApi(reqData.toString())

        val response = client.submitForm(
            url = DOWNLOAD_API,
            formParameters = parameters {
                append("params", params)
                append("encSecKey", encSecKey)
            }
        ) {
            defaultHeaders(REFERER, cookie)
        }

        val json = response.body<JsonObject>()
        val data = json["data"]?.jsonArray?.firstOrNull()?.jsonObject
            ?: throw Exception("Download URL not found")

        val url = data["url"]?.jsonPrimitive?.content
            ?: throw Exception("Download URL is empty")

        return url
    }

    private suspend fun getEApiDownloadURL(songId: String, quality: String): String {
        val headerJson = """{"os":"pc","appver":"","osver":"","deviceId":"pyncm!","requestId":"12345678"}"""

        val payload = buildJsonObject {
            putJsonArray("ids") { add(songId.toInt()) }
            put("level", quality)
            put("encodeType", "flac")
            put("header", headerJson)
        }

        val params = NeteaseCrypto.encryptEApi(DOWNLOAD_EAPI, payload.toString())

        val response = client.submitForm(
            url = DOWNLOAD_EAPI,
            formParameters = parameters {
                append("params", params)
            }
        ) {
            defaultHeaders(REFERER, cookie)
        }

        val json = response.body<JsonObject>()
        val data = json["data"]?.jsonArray?.firstOrNull()?.jsonObject
            ?: throw Exception("EAPI download URL not found")

        return data["url"]?.jsonPrimitive?.content ?: throw Exception("EAPI URL is empty")
    }

    override suspend fun getLyrics(song: Song): String {
        if (song.source != "netease") throw IllegalArgumentException("Source mismatch")

        val songId = song.extra["song_id"] ?: song.id

        val reqData = buildJsonObject {
            put("csrf_token", "")
            put("id", songId)
            put("lv", -1)
            put("tv", -1)
        }

        val (params, encSecKey) = NeteaseCrypto.encryptWeApi(reqData.toString())

        val response = client.submitForm(
            url = LYRIC_API,
            formParameters = parameters {
                append("params", params)
                append("encSecKey", encSecKey)
            }
        ) {
            defaultHeaders(REFERER, cookie)
        }

        val json = response.body<JsonObject>()
        val code = json["code"]?.jsonPrimitive?.long ?: -1
        if (code != 200.toLong()) throw Exception("Netease lyric API error code: $code")

        return json["lrc"]?.jsonObject?.get("lyric")?.jsonPrimitive?.content
            ?: throw Exception("Lyric not found")
    }

    override suspend fun getRecommendedPlaylists(): List<Playlist> {
        val reqData = buildJsonObject {
            put("limit", 30)
            put("total", true)
            put("n", 1000)
        }

        val (params, encSecKey) = NeteaseCrypto.encryptWeApi(reqData.toString())

        val response = client.submitForm(
            url = RECOMMENDED_PLAYLIST_API,
            formParameters = parameters {
                append("params", params)
                append("encSecKey", encSecKey)
            }
        ) {
            defaultHeaders(REFERER, null)
        }

        val json = response.body<JsonObject>()
        val code = json["code"]?.jsonPrimitive?.long ?: -1
        if (code != 200.toLong()) throw Exception("Netease API error code: $code")

        val playlists = mutableListOf<Playlist>()

        json["result"]?.jsonArray?.forEach { plEl ->
            val pl = plEl.jsonObject
            val id = pl["id"]?.jsonPrimitive?.long ?: return@forEach
            val name = pl["name"]?.jsonPrimitive?.content ?: ""
            val picUrl = pl["picUrl"]?.jsonPrimitive?.content ?: ""
            val playCount = pl["playCount"]?.jsonPrimitive?.double?.toLong() ?: 0
            val trackCount = pl["trackCount"]?.jsonPrimitive?.long ?: 0
            val copywriter = pl["copywriter"]?.jsonPrimitive?.content ?: ""

            playlists.add(Playlist(
                id = id.toString(),
                name = name,
                cover = picUrl,
                playCount = playCount,
                trackCount = trackCount,
                description = copywriter,
                creator = if (copywriter.isNotEmpty()) copywriter else "网易云推荐",
                source = "netease",
                link = "https://music.163.com/#/playlist?id=$id"
            ))
        }

        return playlists
    }
}
