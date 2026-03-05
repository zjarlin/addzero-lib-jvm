package site.addzero.network.call.musiclib.api.kuwo

import site.addzero.network.call.musiclib.model.Playlist
import site.addzero.network.call.musiclib.model.PlaylistDetail
import site.addzero.network.call.musiclib.model.Song
import site.addzero.network.call.musiclib.provider.CookieMusicProvider
import site.addzero.network.call.musiclib.utils.HttpClientManager
import site.addzero.network.call.musiclib.utils.defaultHeaders
import site.addzero.network.call.musiclib.utils.timestamp
import io.ktor.client.HttpClient
import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.serialization.json.*

/**
 * 酷我音乐 API 实现
 */
class KuwoAPI(
    override var cookie: String = "",
    private val client: HttpClient = HttpClientManager.client,
) : CookieMusicProvider {

    override val name: String = "酷我音乐"
    override val source: String = "kuwo"

    companion object {
        const val USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36"
    }

    override suspend fun search(keyword: String): List<Song> {
        val response = client.get("http://www.kuwo.cn/search/searchMusicBykeyWord") {
            defaultHeaders(null, cookie)
            url {
                parameters.append("vipver", "1")
                parameters.append("client", "kt")
                parameters.append("ft", "music")
                parameters.append("cluster", "0")
                parameters.append("strategy", "2012")
                parameters.append("encoding", "utf8")
                parameters.append("rformat", "json")
                parameters.append("mobi", "1")
                parameters.append("issubtitle", "1")
                parameters.append("show_copyright_off", "1")
                parameters.append("pn", "0")
                parameters.append("rn", "10")
                parameters.append("all", keyword)
            }
        }

        val json = response.body<JsonObject>()
        val songs = mutableListOf<Song>()

        json["abslist"]?.jsonArray?.forEach { itemEl ->
            val item = itemEl.jsonObject

            val bitSwitch = item["bitSwitch"]?.jsonPrimitive?.long ?: 0
            if (bitSwitch == 0.toLong()) return@forEach

            val musicRID = item["MUSICRID"]?.jsonPrimitive?.content ?: return@forEach
            val cleanID = musicRID.removePrefix("MUSIC_")
            val songName = item["SONGNAME"]?.jsonPrimitive?.content ?: ""
            val artist = item["ARTIST"]?.jsonPrimitive?.content ?: ""
            val album = item["ALBUM"]?.jsonPrimitive?.content ?: ""
            val duration = item["DURATION"]?.jsonPrimitive?.content?.toIntOrNull() ?: 0
            val mInfo = item["MINFO"]?.jsonPrimitive?.content ?: ""
            val cover = item["hts_MVPIC"]?.jsonPrimitive?.content ?: ""

            val size = parseSizeFromMInfo(mInfo)
            val bitrate = parseBitrateFromMInfo(mInfo)

            songs.add(Song(
                id = cleanID,
                name = songName,
                artist = artist,
                album = album,
                duration = duration.toLong(),
                size = size,
                bitrate = bitrate.toLong(),
                source = "kuwo",
                cover = cover,
                link = "http://www.kuwo.cn/play_detail/$cleanID",
                extra = mapOf("rid" to cleanID)
            ))
        }

        return songs
    }

    override suspend fun searchPlaylist(keyword: String): List<Playlist> {
        val response = client.get("http://search.kuwo.cn/r.s") {
            defaultHeaders(null, cookie)
            url {
                parameters.append("all", keyword)
                parameters.append("ft", "playlist")
                parameters.append("itemset", "web_2013")
                parameters.append("client", "kt")
                parameters.append("pcmp4", "1")
                parameters.append("geo", "c")
                parameters.append("vipver", "1")
                parameters.append("pn", "0")
                parameters.append("rn", "10")
                parameters.append("rformat", "json")
                parameters.append("encoding", "utf8")
            }
        }

        var body = response.body<String>()
        body = body.replace("'", "\"")

        val json = Json.parseToJsonElement(body).jsonObject
        val playlists = mutableListOf<Playlist>()

        json["abslist"]?.jsonArray?.forEach { itemEl ->
            val item = itemEl.jsonObject
            val playlistID = item["playlistid"]?.jsonPrimitive?.content ?: return@forEach
            val name = item["name"]?.jsonPrimitive?.content ?: ""
            val pic = item["pic"]?.jsonPrimitive?.content ?: ""
            val songNum = item["songnum"]?.jsonPrimitive?.content ?: "0"
            val intro = item["intro"]?.jsonPrimitive?.content ?: ""
            val nickname = item["nickname"]?.jsonPrimitive?.content ?: ""

            var cover = pic
            if (cover.isNotEmpty()) {
                cover = cover.replace("_150.", "_700.")
                if (!cover.startsWith("http")) {
                    cover = "http://$cover"
                }
            }

            playlists.add(Playlist(
                id = playlistID,
                name = name,
                cover = cover,
                trackCount = (songNum.toIntOrNull() ?: 0).toLong(),
                creator = nickname,
                description = intro,
                source = "kuwo",
                link = "http://www.kuwo.cn/playlist_detail/$playlistID"
            ))
        }

        return playlists
    }

    override suspend fun getPlaylistSongs(id: String): List<Song> {
        val (_, songs) = fetchPlaylistDetail(id)
        return songs
    }

    override suspend fun parsePlaylist(link: String): PlaylistDetail {
        val regex = "playlist_detail/(\\d+)".toRegex()
        val match = regex.find(link) ?: throw IllegalArgumentException("Invalid kuwo playlist link")
        val playlistID = match.groupValues[1]
        return fetchPlaylistDetail(playlistID)
    }

    private suspend fun fetchPlaylistDetail(id: String): PlaylistDetail {
        val response = client.get("http://nplserver.kuwo.cn/pl.svc") {
            defaultHeaders(null, cookie)
            url {
                parameters.append("op", "getlistinfo")
                parameters.append("pid", id)
                parameters.append("pn", "0")
                parameters.append("rn", "100")
                parameters.append("encode", "utf8")
                parameters.append("keyset", "pl2012")
                parameters.append("identity", "kuwo")
                parameters.append("pcmp4", "1")
                parameters.append("vipver", "1")
                parameters.append("newver", "1")
            }
        }

        val json = response.body<JsonObject>()
        val musicList = json["musiclist"]?.jsonArray ?: emptyList()

        if (musicList.isEmpty()) {
            throw Exception("Playlist is empty or ID is invalid")
        }

        val playlist = Playlist(
            id = id,
            source = "kuwo",
            link = "http://www.kuwo.cn/playlist_detail/$id",
            trackCount = musicList.size.toLong()
        )

        val songs = musicList.map { itemEl ->
            val item = itemEl.jsonObject
            val itemId = item["id"]?.jsonPrimitive?.content ?: ""
            var name = item["name"]?.jsonPrimitive?.content ?: ""
            var artist = item["artist"]?.jsonPrimitive?.content ?: ""
            val songName = item["song_name"]?.jsonPrimitive?.content ?: ""
            val artistName = item["artist_name"]?.jsonPrimitive?.content ?: ""
            val album = item["album"]?.jsonPrimitive?.content ?: ""
            val albumPic = item["albumpic"]?.jsonPrimitive?.content ?: ""

            val duration = when (val d = item["duration"]) {
                is JsonPrimitive -> d.content.toIntOrNull() ?: d.intOrNull ?: 0
                else -> 0
            }

            if (name.isEmpty()) name = songName
            if (artist.isEmpty()) artist = artistName

            var cover = albumPic
            if (cover.isNotEmpty() && !cover.startsWith("http")) {
                cover = "http://$cover"
            }
            if (cover.contains("_100.")) {
                cover = cover.replace("_100.", "_500.")
            } else if (cover.contains("_150.")) {
                cover = cover.replace("_150.", "_500.")
            } else if (cover.contains("_120.")) {
                cover = cover.replace("_120.", "_500.")
            }

            Song(
                id = itemId,
                name = name,
                artist = artist,
                album = album,
                duration = duration.toLong(),
                source = "kuwo",
                cover = cover,
                link = "http://www.kuwo.cn/play_detail/$itemId",
                extra = mapOf("rid" to itemId)
            )
        }

        return PlaylistDetail(playlist, songs)
    }

    override suspend fun parse(link: String): Song {
        val regex = "play_detail/(\\d+)".toRegex()
        val match = regex.find(link) ?: throw IllegalArgumentException("Invalid kuwo link")
        val rid = match.groupValues[1]
        return fetchFullSongInfo(rid)
    }

    override suspend fun getDownloadURL(song: Song): String {
        if (song.source != "kuwo") throw IllegalArgumentException("Source mismatch")
        val rid = song.extra["rid"] ?: song.id
        return fetchAudioURL(rid)
    }

    private suspend fun fetchFullSongInfo(rid: String): Song {
        val response = client.get("http://m.kuwo.cn/newh5/singles/songinfoandlrc") {
            defaultHeaders(null, cookie)
            parameter("musicId", rid)
            parameter("httpsStatus", "1")
        }

        val json = response.body<JsonObject>()
        val data = json["data"]?.jsonObject
        val songInfo = data?.get("songinfo")?.jsonObject

        val name = songInfo?.get("songName")?.jsonPrimitive?.content ?: ""
        val artist = songInfo?.get("artist")?.jsonPrimitive?.content ?: ""
        val cover = songInfo?.get("pic")?.jsonPrimitive?.content ?: ""

        val audioURL = fetchAudioURL(rid)

        return Song(
            id = rid,
            name = name,
            artist = artist,
            source = "kuwo",
            cover = cover,
            url = audioURL,
            link = "http://www.kuwo.cn/play_detail/$rid",
            extra = mapOf("rid" to rid)
        )
    }

    private suspend fun fetchAudioURL(rid: String): String {
        val qualities = listOf("128kmp3", "320kmp3", "flac", "2000kflac")
        val randomID = "C_APK_guanwang_${timestamp()}${(0..999999).random()}"

        for (br in qualities) {
            val response = client.get("https://mobi.kuwo.cn/mobi.s") {
                defaultHeaders(null, cookie)
                url {
                    parameters.append("f", "web")
                    parameters.append("source", "kwplayercar_ar_6.0.0.9_B_jiakong_vh.apk")
                    parameters.append("from", "PC")
                    parameters.append("type", "convert_url_with_sign")
                    parameters.append("br", br)
                    parameters.append("rid", rid)
                    parameters.append("user", randomID)
                }
            }

            val json = try {
                response.body<JsonObject>()
            } catch (e: Exception) {
                continue
            }

            val url = json["data"]?.jsonObject?.get("url")?.jsonPrimitive?.content
            if (!url.isNullOrEmpty()) {
                return url
            }
        }

        throw Exception("Download URL not found")
    }

    override suspend fun getLyrics(song: Song): String {
        if (song.source != "kuwo") throw IllegalArgumentException("Source mismatch")
        val rid = song.extra["rid"] ?: song.id

        val response = client.get("http://m.kuwo.cn/newh5/singles/songinfoandlrc") {
            defaultHeaders(null, cookie)
            parameter("musicId", rid)
            parameter("httpsStatus", "1")
        }

        val json = response.body<JsonObject>()
        val lrclist = json["data"]?.jsonObject?.get("lrclist")?.jsonArray ?: emptyList()

        val sb = StringBuilder()
        lrclist.forEach { lineEl ->
            val line = lineEl.jsonObject
            val timeStr = line["time"]?.jsonPrimitive?.content ?: return@forEach
            val lineLyric = line["lineLyric"]?.jsonPrimitive?.content ?: ""

            val secs = timeStr.toDoubleOrNull() ?: 0.0
            val m = (secs / 60).toInt()
            val s = (secs % 60).toInt()
            val ms = ((secs - secs.toInt()) * 100).toInt()

            sb.appendLine("[%02d:%02d.%02d]$lineLyric".format(m, s, ms))
        }

        return sb.toString()
    }

    override suspend fun getRecommendedPlaylists(): List<Playlist> {
        val response = client.get("http://wapi.kuwo.cn/api/pc/classify/playlist/getRcmPlayList") {
            defaultHeaders(null, cookie)
            parameter("pn", "0")
            parameter("rn", "30")
            parameter("order", "hot")
        }

        val json = response.body<JsonObject>()
        val code = json["code"]?.jsonPrimitive?.long ?: -1
        if (code != 200.toLong()) throw Exception("Kuwo API error code: $code")

        val playlists = mutableListOf<Playlist>()

        json["data"]?.jsonObject?.get("data")?.jsonArray?.forEach { itemEl ->
            val item = itemEl.jsonObject
            val id = item["id"]?.jsonPrimitive?.content ?: return@forEach
            val name = item["name"]?.jsonPrimitive?.content ?: ""
            val img = item["img"]?.jsonPrimitive?.content ?: ""
            val listenCnt = item["listencnt"]
            val songNum = item["songnum"] ?: item["total"] ?: item["count"] ?: item["musicnum"]
            val userName = item["uname"]?.jsonPrimitive?.content ?: ""
            val desc = item["desc"]?.jsonPrimitive?.content ?: ""

            val cover = if (img.isNotEmpty() && !img.startsWith("http")) "http://$img" else img

            val playCount = when (listenCnt) {
                is JsonPrimitive -> listenCnt.content.toIntOrNull() ?: listenCnt.intOrNull ?: 0
                else -> 0
            }

            val trackCount = when (songNum) {
                is JsonPrimitive -> songNum.content.toIntOrNull() ?: songNum.intOrNull ?: 0
                else -> 0
            }

            playlists.add(Playlist(
                id = id,
                name = name,
                cover = cover,
                playCount = playCount.toLong(),
                trackCount = trackCount.toLong(),
                creator = userName,
                description = desc,
                source = "kuwo",
                link = "http://www.kuwo.cn/playlist_detail/$id"
            ))
        }

        return playlists
    }

    private fun parseSizeFromMInfo(minfo: String): Long {
        if (minfo.isEmpty()) return 0
        // 简化的解析逻辑
        val sizeRegex = "size:([0-9.]+)([MG]?)".toRegex()
        val match = sizeRegex.find(minfo) ?: return 0
        val sizeStr = match.groupValues[1]
        val unit = match.groupValues[2]
        val size = sizeStr.toDoubleOrNull() ?: return 0
        return when (unit) {
            "M" -> (size * 1024 * 1024).toLong()
            "G" -> (size * 1024 * 1024 * 1024).toLong()
            else -> size.toLong()
        }
    }

    private fun parseBitrateFromMInfo(minfo: String): Int {
        if (minfo.isEmpty()) return 128
        // 简化的解析逻辑
        return when {
            minfo.contains("320") -> 320
            minfo.contains("2000") -> 2000
            minfo.contains("flac") -> 800
            else -> 128
        }
    }
}
