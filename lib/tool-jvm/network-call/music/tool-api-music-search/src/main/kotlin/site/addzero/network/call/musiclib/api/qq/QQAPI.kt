package site.addzero.network.call.musiclib.api.qq

import site.addzero.network.call.musiclib.model.Playlist
import site.addzero.network.call.musiclib.model.PlaylistDetail
import site.addzero.network.call.musiclib.model.Song
import site.addzero.network.call.musiclib.provider.CookieMusicProvider
import site.addzero.network.call.musiclib.utils.HttpClientManager
import site.addzero.network.call.musiclib.utils.generateGuid
import site.addzero.network.call.musiclib.utils.mobileHeaders
import io.ktor.client.HttpClient
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.json.*

/**
 * QQ音乐 API 实现
 */
class QQAPI(
    override var cookie: String = "",
    private val client: HttpClient = HttpClientManager.client,
) : CookieMusicProvider {

    private var isVipCache: Boolean? = null

    override val name: String = "QQ音乐"
    override val source: String = "qq"

    companion object {
        const val USER_AGENT = "Mozilla/5.0 (iPhone; CPU iPhone OS 9_1 like Mac OS X) AppleWebKit/601.1.46 (KHTML, like Gecko) Version/9.0 Mobile/13B143 Safari/601.1"
        const val SEARCH_REFERER = "http://m.y.qq.com"
        const val DOWNLOAD_REFERER = "http://y.qq.com"
        const val LYRIC_REFERER = "https://y.qq.com/portal/player.html"
    }

    override suspend fun isVipAccount(): Boolean {
        isVipCache?.let { return it }
        if (cookie.isEmpty()) {
            isVipCache = false
            return false
        }

        val guid = generateGuid()
        val songMID = "004YZbkL2MNHoY" // 周杰伦 - 晴天 (VIP歌曲)
        val filename = "M500${songMID}${songMID}.mp3"

        val reqData = buildJsonObject {
            putJsonObject("comm") {
                put("cv", 4747474)
                put("ct", 24)
                put("format", "json")
                put("inCharset", "utf-8")
                put("outCharset", "utf-8")
                put("notice", 0)
                put("platform", "yqq.json")
                put("needNewCode", 1)
                put("uin", 0)
            }
            putJsonObject("req_1") {
                put("module", "music.vkey.GetVkey")
                put("method", "UrlGetVkey")
                putJsonObject("param") {
                    put("guid", guid)
                    putJsonArray("songmid") { add(songMID) }
                    putJsonArray("songtype") { add(0) }
                    put("uin", "0")
                    put("loginflag", 1)
                    put("platform", "20")
                    putJsonArray("filename") { add(filename) }
                }
            }
        }

        val response = client.post("https://u.y.qq.com/cgi-bin/musicu.fcg") {
            mobileHeaders(DOWNLOAD_REFERER, cookie)
            contentType(ContentType.Application.Json)
            setBody(reqData.toString())
        }

        val json = response.body<JsonObject>()
        val midUrlInfo = json["req_1"]?.jsonObject?.get("data")
            ?.jsonObject?.get("midurlinfo")?.jsonArray

        val isVip = midUrlInfo?.firstOrNull()?.jsonObject?.get("purl")?.jsonPrimitive?.content?.isNotEmpty() == true
        isVipCache = isVip
        return isVip
    }

    override suspend fun search(keyword: String): List<Song> {
        val response = client.get("http://c.y.qq.com/soso/fcgi-bin/search_for_qq_cp") {
            mobileHeaders(SEARCH_REFERER, cookie)
            url {
                parameters.append("w", keyword)
                parameters.append("format", "json")
                parameters.append("p", "1")
                parameters.append("n", "10")
            }
        }

        val json = response.body<JsonObject>()
        val songs = mutableListOf<Song>()
        val isVip = isVipAccount()

        json["data"]?.jsonObject?.get("song")?.jsonObject?.get("list")?.jsonArray?.forEach { itemEl ->
            val item = itemEl.jsonObject

            // 过滤付费歌曲
            val payPlay = item["pay"]?.jsonObject?.get("payplay")?.jsonPrimitive?.long ?: 0
            if (!isVip && payPlay == 1.toLong()) return@forEach

            val songID = item["songid"]?.jsonPrimitive?.long ?: return@forEach
            val songName = item["songname"]?.jsonPrimitive?.content ?: ""
            val songMID = item["songmid"]?.jsonPrimitive?.content ?: ""
            val albumName = item["albumname"]?.jsonPrimitive?.content ?: ""
            val albumMID = item["albummid"]?.jsonPrimitive?.content ?: ""
            val interval = item["interval"]?.jsonPrimitive?.long ?: 0
            val size128 = item["size128"]?.jsonPrimitive?.long ?: 0
            val size320 = item["size320"]?.jsonPrimitive?.long ?: 0
            val sizeFlac = item["sizeflac"]?.jsonPrimitive?.long ?: 0

            val singers = item["singer"]?.jsonArray?.map {
                it.jsonObject["name"]?.jsonPrimitive?.content ?: ""
            } ?: emptyList()

            val coverURL = if (albumMID.isNotEmpty()) {
                "https://y.gtimg.cn/music/photo_new/T002R300x300M000$albumMID.jpg"
            } else ""

            val (fileSize, bitrate) = when {
                sizeFlac > 0 -> Pair(sizeFlac, if (interval > 0) (sizeFlac * 8 / 1000 / interval).toInt() else 800)
                size320 > 0 -> Pair(size320, 320)
                else -> Pair(size128, 128)
            }

            songs.add(Song(
                id = songMID,
                name = songName,
                artist = singers.joinToString("、"),
                album = albumName,
                duration = interval,
                size = fileSize,
                bitrate = bitrate.toLong(),
                source = "qq",
                cover = coverURL,
                link = "https://y.qq.com/n/ryqq/songDetail/$songMID",
                extra = mapOf("songmid" to songMID)
            ))
        }

        return songs
    }

    override suspend fun searchPlaylist(keyword: String): List<Playlist> {
        val response = client.get("http://c.y.qq.com/soso/fcgi-bin/client_music_search_songlist") {
            mobileHeaders(SEARCH_REFERER, cookie)
            url {
                parameters.append("query", keyword)
                parameters.append("page_no", "0")
                parameters.append("num_per_page", "20")
                parameters.append("format", "json")
                parameters.append("remoteplace", "txt.yqq.playlist")
                parameters.append("flag_qc", "0")
            }
        }

        var body = response.body<String>()
        // 处理JSONP
        if (body.startsWith("(") && body.endsWith(")")) {
            body = body.substring(1, body.length - 1)
        }

        val json = Json.parseToJsonElement(body).jsonObject
        val playlists = mutableListOf<Playlist>()

        json["data"]?.jsonObject?.get("list")?.jsonArray?.forEach { itemEl ->
            val item = itemEl.jsonObject
            val dissID = item["dissid"]?.jsonPrimitive?.content ?: return@forEach
            val dissName = item["dissname"]?.jsonPrimitive?.content ?: ""
            val imgUrl = item["imgurl"]?.jsonPrimitive?.content ?: ""
            val songCount = item["song_count"]?.jsonPrimitive?.long ?: 0
            val listenNum = item["listennum"]?.jsonPrimitive?.long ?: 0
            val creator = item["creator"]?.jsonObject?.get("name")?.jsonPrimitive?.content ?: ""

            val cover = imgUrl.replace("http://", "https://")

            playlists.add(Playlist(
                id = dissID,
                name = dissName,
                cover = cover,
                trackCount = songCount,
                playCount = listenNum,
                creator = creator,
                source = "qq",
                link = "https://y.qq.com/n/ryqq/playlist/$dissID"
            ))
        }

        return playlists
    }

    override suspend fun getPlaylistSongs(id: String): List<Song> {
        val (_, songs) = fetchPlaylistDetail(id)
        return songs
    }

    override suspend fun parsePlaylist(link: String): PlaylistDetail {
        val regex = "playlist/(\\d+)".toRegex()
        val match = regex.find(link) ?: throw IllegalArgumentException("Invalid QQ playlist link")
        val dissid = match.groupValues[1]
        return fetchPlaylistDetail(dissid)
    }

    private suspend fun fetchPlaylistDetail(id: String): PlaylistDetail {
        val response = client.get("http://c.y.qq.com/qzone/fcg-bin/fcg_ucc_getcdinfo_byids_cp.fcg") {
            mobileHeaders(DOWNLOAD_REFERER, cookie)
            url {
                parameters.append("type", "1")
                parameters.append("json", "1")
                parameters.append("utf8", "1")
                parameters.append("onlysong", "0")
                parameters.append("disstid", id)
                parameters.append("format", "json")
                parameters.append("g_tk", "5381")
                parameters.append("loginUin", "0")
                parameters.append("hostUin", "0")
                parameters.append("inCharset", "utf8")
                parameters.append("outCharset", "utf-8")
                parameters.append("notice", "0")
                parameters.append("platform", "yqq")
                parameters.append("needNewCode", "0")
            }
        }

        var body = response.body<String>()
        // 处理JSONP
        if (body.contains("(") && body.endsWith(")")) {
            val start = body.indexOf("(")
            body = body.substring(start + 1, body.length - 1)
        }

        val json = Json.parseToJsonElement(body).jsonObject
        val cdlist = json["cdlist"]?.jsonArray
            ?: throw Exception("Playlist not found (empty cdlist)")

        val info = cdlist.firstOrNull()?.jsonObject
            ?: throw Exception("Playlist not found")

        val playlist = Playlist(
            id = id,
            name = info["dissname"]?.jsonPrimitive?.content ?: "",
            cover = info["logo"]?.jsonPrimitive?.content ?: "",
            creator = info["nickname"]?.jsonPrimitive?.content ?: "",
            description = info["desc"]?.jsonPrimitive?.content ?: "",
            playCount = info["visitnum"]?.jsonPrimitive?.long ?: 0,
            trackCount = info["songnum"]?.jsonPrimitive?.long ?: 0,
            source = "qq",
            link = "https://y.qq.com/n/ryqq/playlist/$id"
        )

        val songs = mutableListOf<Song>()

        info["songlist"]?.jsonArray?.forEach { itemEl ->
            val item = itemEl.jsonObject
            val songID = item["songid"]?.jsonPrimitive?.long ?: return@forEach
            val songName = item["songname"]?.jsonPrimitive?.content ?: ""
            val songMID = item["songmid"]?.jsonPrimitive?.content ?: ""
            val albumName = item["albumname"]?.jsonPrimitive?.content ?: ""
            val albumMID = item["albummid"]?.jsonPrimitive?.content ?: ""
            val interval = item["interval"]?.jsonPrimitive?.long ?: 0
            val size128 = item["size128"]?.jsonPrimitive?.long ?: 0
            val size320 = item["size320"]?.jsonPrimitive?.long ?: 0
            val sizeFlac = item["sizeflac"]?.jsonPrimitive?.long ?: 0

            val singers = item["singer"]?.jsonArray?.map {
                it.jsonObject["name"]?.jsonPrimitive?.content ?: ""
            } ?: emptyList()

            val coverURL = if (albumMID.isNotEmpty()) {
                "https://y.gtimg.cn/music/photo_new/T002R300x300M000$albumMID.jpg"
            } else ""

            val (fileSize, bitrate) = when {
                sizeFlac > 0 -> Pair(sizeFlac, if (interval > 0) (sizeFlac * 8 / 1000 / interval).toInt() else 800)
                size320 > 0 -> Pair(size320, 320)
                else -> Pair(size128, 128)
            }

            songs.add(Song(
                id = songMID,
                name = songName,
                artist = singers.joinToString("、"),
                album = albumName,
                duration = interval,
                size = fileSize,
                bitrate = bitrate.toLong(),
                source = "qq",
                cover = coverURL,
                link = "https://y.qq.com/n/ryqq/songDetail/$songMID",
                extra = mapOf("songmid" to songMID)
            ))
        }

        return PlaylistDetail(playlist, songs)
    }

    override suspend fun parse(link: String): Song {
        val regex = "songDetail/(\\w+)".toRegex()
        val match = regex.find(link) ?: throw IllegalArgumentException("Invalid QQ music link")
        val songMID = match.groupValues[1]

        val song = fetchSongDetail(songMID)
        val downloadURL = getDownloadURL(song)
        return song.copy(url = downloadURL)
    }

    private suspend fun fetchSongDetail(songMID: String): Song {
        val response = client.get("https://c.y.qq.com/v8/fcg-bin/fcg_play_single_song.fcg") {
            mobileHeaders(SEARCH_REFERER, cookie)
            url {
                parameters.append("songmid", songMID)
                parameters.append("format", "json")
            }
        }

        val json = response.body<JsonObject>()
        val data = json["data"]?.jsonArray?.firstOrNull()?.jsonObject
            ?: throw Exception("Song detail not found")

        val id = data["id"]?.jsonPrimitive?.long ?: 0
        val name = data["name"]?.jsonPrimitive?.content ?: ""
        val mid = data["mid"]?.jsonPrimitive?.content ?: ""
        val album = data["album"]?.jsonObject
        val albumName = album?.get("name")?.jsonPrimitive?.content ?: ""
        val albumMID = album?.get("mid")?.jsonPrimitive?.content ?: ""
        val interval = data["interval"]?.jsonPrimitive?.long ?: 0

        val singers = data["singer"]?.jsonArray?.map {
            it.jsonObject["name"]?.jsonPrimitive?.content ?: ""
        } ?: emptyList()

        val coverURL = if (albumMID.isNotEmpty()) {
            "https://y.gtimg.cn/music/photo_new/T002R300x300M000$albumMID.jpg"
        } else ""

        return Song(
            id = mid,
            name = name,
            artist = singers.joinToString("、"),
            album = albumName,
            duration = interval,
            source = "qq",
            cover = coverURL,
            link = "https://y.qq.com/n/ryqq/songDetail/$mid",
            extra = mapOf("songmid" to mid)
        )
    }

    override suspend fun getDownloadURL(song: Song): String {
        if (song.source != "qq") throw IllegalArgumentException("Source mismatch")

        val songMID = song.extra["songmid"] ?: song.id
        val guid = generateGuid()

        val prefixes: List<String>
        val exts: List<String>

        if (isVipAccount()) {
            prefixes = listOf("AI00", "Q001", "Q000", "F000", "O801", "M800", "M500")
            exts = listOf("flac", "flac", "flac", "flac", "ogg", "mp3", "mp3")
        } else {
            prefixes = listOf("M800", "M500")
            exts = listOf("mp3", "mp3")
        }

        val filenames = prefixes.map { "$it$songMID$songMID.${exts[prefixes.indexOf(it)]}" }
        val songmids = List(prefixes.size) { songMID }
        val songtypes = List(prefixes.size) { 0 }

        val reqData = buildJsonObject {
            putJsonObject("comm") {
                put("cv", 4747474)
                put("ct", 24)
                put("format", "json")
                put("inCharset", "utf-8")
                put("outCharset", "utf-8")
                put("notice", 0)
                put("platform", "yqq.json")
                put("needNewCode", 1)
                put("uin", 0)
            }
            putJsonObject("req_1") {
                put("module", "music.vkey.GetVkey")
                put("method", "UrlGetVkey")
                putJsonObject("param") {
                    put("guid", guid)
                    putJsonArray("songmid") { songmids.forEach { add(it) } }
                    putJsonArray("songtype") { songtypes.forEach { add(it) } }
                    put("uin", "0")
                    put("loginflag", 1)
                    put("platform", "20")
                    putJsonArray("filename") { filenames.forEach { add(it) } }
                }
            }
        }

        val response = client.post("https://u.y.qq.com/cgi-bin/musicu.fcg") {
            mobileHeaders(DOWNLOAD_REFERER, cookie)
            contentType(ContentType.Application.Json)
            setBody(reqData.toString())
        }

        val json = response.body<JsonObject>()
        val midUrlInfo = json["req_1"]?.jsonObject?.get("data")
            ?.jsonObject?.get("midurlinfo")?.jsonArray ?: emptyList()

        for (expectedFilename in filenames) {
            for (info in midUrlInfo) {
                val infoObj = info.jsonObject
                val filename = infoObj["filename"]?.jsonPrimitive?.content
                val purl = infoObj["purl"]?.jsonPrimitive?.content
                if (filename == expectedFilename && !purl.isNullOrEmpty()) {
                    return "https://ws.stream.qqmusic.qq.com/$purl"
                }
            }
        }

        throw Exception("No valid download URL found or VIP required")
    }

    override suspend fun getLyrics(song: Song): String {
        if (song.source != "qq") throw IllegalArgumentException("Source mismatch")

        val songMID = song.extra["songmid"] ?: song.id

        val response = client.get("https://c.y.qq.com/lyric/fcgi-bin/fcg_query_lyric_new.fcg") {
            mobileHeaders(LYRIC_REFERER, cookie)
            url {
                parameters.append("songmid", songMID)
                parameters.append("loginUin", "0")
                parameters.append("hostUin", "0")
                parameters.append("format", "json")
                parameters.append("inCharset", "utf8")
                parameters.append("outCharset", "utf-8")
                parameters.append("notice", "0")
                parameters.append("platform", "yqq.json")
                parameters.append("needNewCode", "0")
            }
        }

        var body = response.body<String>()
        // 处理JSONP
        if (body.contains("(") && body.endsWith(")")) {
            val start = body.indexOf("(")
            body = body.substring(start + 1, body.lastIndexOf(")"))
        }

        val json = Json.parseToJsonElement(body).jsonObject
        val lyric = json["lyric"]?.jsonPrimitive?.content
            ?: throw Exception("Lyric is empty or not found")

        return String(java.util.Base64.getDecoder().decode(lyric))
    }

    override suspend fun getRecommendedPlaylists(): List<Playlist> {
        val reqData = buildJsonObject {
            putJsonObject("comm") {
                put("ct", 24)
            }
            putJsonObject("recomPlaylist") {
                put("method", "get_hot_recommend")
                put("module", "playlist.HotRecommendServer")
                putJsonObject("param") {
                    put("async", 1)
                    put("cmd", 2)
                }
            }
        }

        val response = client.post("https://u.y.qq.com/cgi-bin/musicu.fcg") {
            mobileHeaders(DOWNLOAD_REFERER, cookie)
            contentType(ContentType.Application.Json)
            setBody(reqData.toString())
        }

        val json = response.body<JsonObject>()
        val code = json["code"]?.jsonPrimitive?.long ?: -1
        if (code != 0.toLong()) throw Exception("QQ API error code: $code")

        val playlists = mutableListOf<Playlist>()

        json["recomPlaylist"]?.jsonObject?.get("data")?.jsonObject?.get("v_hot")?.jsonArray?.forEach { itemEl ->
            val item = itemEl.jsonObject
            val contentID = item["content_id"]?.jsonPrimitive?.long ?: return@forEach
            val title = item["title"]?.jsonPrimitive?.content ?: ""
            val cover = item["cover"]?.jsonPrimitive?.content ?: "".replace("http://", "https://")
            val listenNum = item["listen_num"]?.jsonPrimitive?.long ?: 0
            val songCnt = item["song_cnt"]?.jsonPrimitive?.long
                ?: item["song_count"]?.jsonPrimitive?.long ?: 0
            val username = item["username"]?.jsonPrimitive?.content ?: ""

            playlists.add(Playlist(
                id = contentID.toString(),
                name = title,
                cover = cover,
                playCount = listenNum,
                trackCount = songCnt,
                creator = username,
                source = "qq",
                link = "https://y.qq.com/n/ryqq/playlist/$contentID"
            ))
        }

        return playlists
    }
}
