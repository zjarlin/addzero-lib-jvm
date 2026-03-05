package site.addzero.network.call.musiclib.api.qianqian

import site.addzero.network.call.musiclib.crypto.CommonCrypto
import site.addzero.network.call.musiclib.model.Playlist
import site.addzero.network.call.musiclib.model.PlaylistDetail
import site.addzero.network.call.musiclib.model.Song
import site.addzero.network.call.musiclib.provider.CookieMusicProvider
import site.addzero.network.call.musiclib.utils.HttpClientManager
import site.addzero.network.call.musiclib.utils.defaultHeaders
import site.addzero.network.call.musiclib.utils.timestampSeconds
import io.ktor.client.HttpClient
import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.serialization.json.*

/**
 * 千千音乐 API 实现
 */
class QianqianAPI(
    override var cookie: String = "",
    private val client: HttpClient = HttpClientManager.client,
) : CookieMusicProvider {

    override val name: String = "千千音乐"
    override val source: String = "qianqian"

    companion object {
        const val APP_ID = "16073360"
        const val SECRET = "0b50b02fd0d73a9c4c8c3a781c30845f"
        const val USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36"
        const val REFERER = "https://music.91q.com/player"
    }

    override suspend fun search(keyword: String): List<Song> {
        val params = mutableMapOf(
            "word" to keyword,
            "type" to "1",
            "pageNo" to "1",
            "pageSize" to "10",
            "appid" to APP_ID
        )
        signParams(params)

        val response = client.get("https://music.91q.com/v1/search") {
            defaultHeaders(REFERER, cookie)
            url {
                params.forEach { (k, v) -> parameters.append(k, v) }
            }
        }

        val json = response.body<JsonObject>()
        val songs = mutableListOf<Song>()

        json["data"]?.jsonObject?.get("typeTrack")?.jsonArray?.forEach { itemEl ->
            val item = itemEl.jsonObject

            val isVip = item["isVip"]?.jsonPrimitive?.long ?: 0
            if (isVip != 0.toLong()) return@forEach

            val tsid = item["TSID"]?.jsonPrimitive?.content ?: return@forEach
            val title = item["title"]?.jsonPrimitive?.content ?: ""
            val albumTitle = item["albumTitle"]?.jsonPrimitive?.content ?: ""
            val pic = item["pic"]?.jsonPrimitive?.content ?: ""
            val duration = item["duration"]?.jsonPrimitive?.long ?: 0

            val artists = item["artist"]?.jsonArray?.map {
                it.jsonObject["name"]?.jsonPrimitive?.content ?: ""
            } ?: emptyList()

            val rateFileInfo = item["rateFileInfo"]?.jsonObject
            var size: Long = 0
            var bitrate: Long = 0

            listOf("3000", "320", "128", "64").forEach { rate ->
                if (size > 0) return@forEach
                val info = rateFileInfo?.get(rate)?.jsonObject
                val infoSize = info?.get("size")?.jsonPrimitive?.long ?: 0
                if (infoSize > 0) {
                    size = infoSize
                    bitrate = (if (duration > 0) (size * 8 / 1000 / duration).toInt()
                    else rate.toIntOrNull() ?: 128).toLong()
                }
            }

            songs.add(Song(
                id = tsid,
                name = title,
                artist = artists.joinToString("、"),
                album = albumTitle,
                duration = duration,
                size = size,
                bitrate = bitrate,
                source = "qianqian",
                cover = pic,
                link = "https://music.91q.com/song/$tsid",
                extra = mapOf("tsid" to tsid)
            ))
        }

        return songs
    }

    override suspend fun searchPlaylist(keyword: String): List<Playlist> {
        val params = mutableMapOf(
            "word" to keyword,
            "type" to "6",
            "pageNo" to "1",
            "pageSize" to "10",
            "appid" to APP_ID,
            "timestamp" to timestampSeconds()
        )
        signParams(params)

        val response = client.get("https://music.91q.com/v1/search") {
            defaultHeaders(REFERER, cookie)
            url { params.forEach { (k, v) -> parameters.append(k, v) } }
        }

        val json = response.body<JsonObject>()
        val playlists = mutableListOf<Playlist>()

        if (json["state"]?.jsonPrimitive?.boolean != true) return emptyList()

        val data = json["data"]?.jsonObject
        data?.get("typeSonglist")?.jsonArray?.forEach { itemEl ->
            val item = itemEl.jsonObject
            val id = item["id"]
            val idStr = when (id) {
                is JsonPrimitive -> id.content
                else -> return@forEach
            }
            val title = item["title"]?.jsonPrimitive?.content ?: ""
            val pic = item["pic"]?.jsonPrimitive?.content ?: ""
            val trackCount = item["trackCount"]?.jsonPrimitive?.long ?: 0
            val tag = item["tag"]?.jsonPrimitive?.content ?: ""

            playlists.add(Playlist(
                id = idStr,
                name = title,
                cover = pic,
                trackCount = trackCount,
                description = tag,
                source = "qianqian"
            ))
        }

        return playlists
    }

    override suspend fun getPlaylistSongs(id: String): List<Song> {
        val params = mutableMapOf(
            "id" to id,
            "appid" to APP_ID,
            "type" to "0"
        )
        signParams(params)

        val response = client.get("https://music.91q.com/v1/tracklist/info") {
            defaultHeaders(REFERER, cookie)
            url { params.forEach { (k, v) -> parameters.append(k, v) } }
        }

        val json = response.body<JsonObject>()
        val songs = mutableListOf<Song>()

        val errno = json["errno"]?.jsonPrimitive?.long ?: 0
        if (errno != 0.toLong() && errno != 22000.toLong()) {
            throw Exception("API error: ${json["errmsg"]?.jsonPrimitive?.content}")
        }

        json["data"]?.jsonObject?.get("trackList")?.jsonArray?.forEach { itemEl ->
            val item = itemEl.jsonObject
            val tsid = item["TSID"]?.jsonPrimitive?.content ?: return@forEach
            val title = item["title"]?.jsonPrimitive?.content ?: ""
            val albumTitle = item["albumTitle"]?.jsonPrimitive?.content ?: ""
            val pic = item["pic"]?.jsonPrimitive?.content ?: ""
            val duration = item["duration"]?.jsonPrimitive?.long ?: 0

            val artists = item["artist"]?.jsonArray?.map {
                it.jsonObject["name"]?.jsonPrimitive?.content ?: ""
            } ?: emptyList()

            songs.add(Song(
                id = tsid,
                name = title,
                artist = artists.joinToString("、"),
                album = albumTitle,
                duration = duration,
                source = "qianqian",
                cover = pic,
                link = "https://music.91q.com/song/$tsid",
                extra = mapOf("tsid" to tsid)
            ))
        }

        return songs
    }

    override suspend fun parsePlaylist(link: String): PlaylistDetail {
        throw NotImplementedError("Qianqian playlist parsing not implemented")
    }

    override suspend fun parse(link: String): Song {
        val regex = "music\\.91q\\.com/song/(\\w+)".toRegex()
        val match = regex.find(link) ?: throw IllegalArgumentException("Invalid qianqian link")
        val tsid = match.groupValues[1]

        val song = fetchSongInfo(tsid)
        val url = fetchDownloadURL(tsid)
        return song.copy(url = url)
    }

    override suspend fun getDownloadURL(song: Song): String {
        if (song.source != "qianqian") throw IllegalArgumentException("Source mismatch")
        val tsid = song.extra["tsid"] ?: song.id
        return fetchDownloadURL(tsid)
    }

    private suspend fun fetchDownloadURL(tsid: String): String {
        listOf("3000", "320", "128", "64").forEach { rate ->
            val params = mutableMapOf(
                "TSID" to tsid,
                "appid" to APP_ID,
                "rate" to rate
            )
            signParams(params)

            val response = client.get("https://music.91q.com/v1/song/tracklink") {
                defaultHeaders(REFERER, cookie)
                url { params.forEach { (k, v) -> parameters.append(k, v) } }
            }

            val json = try {
                response.body<JsonObject>()
            } catch (e: Exception) {
                return@forEach
            }

            val path = json["data"]?.jsonObject?.get("path")?.jsonPrimitive?.content
            if (!path.isNullOrEmpty()) return path

            val trailPath = json["data"]?.jsonObject?.get("trail_audio_info")
                ?.jsonObject?.get("path")?.jsonPrimitive?.content
            if (!trailPath.isNullOrEmpty()) return trailPath
        }

        throw Exception("Download URL not found")
    }

    private suspend fun fetchSongInfo(tsid: String): Song {
        val params = mutableMapOf(
            "TSID" to tsid,
            "appid" to APP_ID
        )
        signParams(params)

        val response = client.get("https://music.91q.com/v1/song/info") {
            defaultHeaders(REFERER, cookie)
            url { params.forEach { (k, v) -> parameters.append(k, v) } }
        }

        val json = response.body<JsonObject>()
        val data = json["data"]?.jsonArray?.firstOrNull()?.jsonObject
            ?: throw Exception("Song info not found")

        val title = data["title"]?.jsonPrimitive?.content ?: ""
        val albumTitle = data["albumTitle"]?.jsonPrimitive?.content ?: ""
        val pic = data["pic"]?.jsonPrimitive?.content ?: ""
        val duration = data["duration"]?.jsonPrimitive?.long ?: 0

        val artists = data["artist"]?.jsonArray?.map {
            it.jsonObject["name"]?.jsonPrimitive?.content ?: ""
        } ?: emptyList()

        return Song(
            id = tsid,
            name = title,
            artist = artists.joinToString("、"),
            album = albumTitle,
            duration = duration,
            source = "qianqian",
            cover = pic,
            link = "https://music.91q.com/song/$tsid",
            extra = mapOf("tsid" to tsid)
        )
    }

    override suspend fun getLyrics(song: Song): String {
        if (song.source != "qianqian") throw IllegalArgumentException("Source mismatch")
        val tsid = song.extra["tsid"] ?: song.id

        val params = mutableMapOf(
            "TSID" to tsid,
            "appid" to APP_ID
        )
        signParams(params)

        val response = client.get("https://music.91q.com/v1/song/info") {
            defaultHeaders(REFERER, cookie)
            url { params.forEach { (k, v) -> parameters.append(k, v) } }
        }

        val json = response.body<JsonObject>()
        val data = json["data"]?.jsonArray?.firstOrNull()?.jsonObject
            ?: throw Exception("Song info not found")

        val lyricURL = data["lyric"]?.jsonPrimitive?.content
            ?: throw Exception("Lyric URL not found")

        val lyricResponse = client.get(lyricURL) {
            defaultHeaders(REFERER, cookie)
        }

        return lyricResponse.body<String>()
    }

    private fun signParams(params: MutableMap<String, String>) {
        params["timestamp"] = timestampSeconds()
        val sorted = params.toSortedMap()
        val signStr = sorted.entries.joinToString("&") { "${it.key}=${it.value}" } + SECRET
        params["sign"] = CommonCrypto.md5(signStr)
    }
}
