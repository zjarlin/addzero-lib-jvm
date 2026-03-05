package site.addzero.network.call.musiclib.api.soda

import site.addzero.network.call.musiclib.crypto.SodaCrypto
import site.addzero.network.call.musiclib.model.Playlist
import site.addzero.network.call.musiclib.model.PlaylistDetail
import site.addzero.network.call.musiclib.model.Song
import site.addzero.network.call.musiclib.provider.CookieMusicProvider
import site.addzero.network.call.musiclib.utils.HttpClientManager
import io.ktor.client.HttpClient
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.json.*
import java.net.URLDecoder
import java.net.URLEncoder

/**
 * 汽水音乐 API 实现
 */
class SodaAPI(
    override var cookie: String = "",
    private val client: HttpClient = HttpClientManager.client,
) : CookieMusicProvider {

    override val name: String = "汽水音乐"
    override val source: String = "soda"

    companion object {
        const val USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36"
    }

    private fun HttpRequestBuilder.sodaHeaders() {
        header(HttpHeaders.UserAgent, USER_AGENT)
        header(HttpHeaders.Cookie, cookie)
    }

    override suspend fun search(keyword: String): List<Song> {
        val response = client.get("https://api.qishui.com/luna/pc/search/track") {
            sodaHeaders()
            url {
                parameters.append("q", keyword)
                parameters.append("cursor", "0")
                parameters.append("search_method", "input")
                parameters.append("aid", "386088")
                parameters.append("device_platform", "web")
                parameters.append("channel", "pc_web")
            }
        }

        val json = response.body<JsonObject>()
        val songs = mutableListOf<Song>()

        val resultGroups = json["result_groups"]?.jsonArray
        if (resultGroups.isNullOrEmpty()) return emptyList()

        resultGroups[0].jsonObject["data"]?.jsonArray?.forEach { itemEl ->
            val item = itemEl.jsonObject["entity"]?.jsonObject?.get("track_wrapper")
                ?.jsonObject?.get("track")?.jsonObject ?: return@forEach

            val trackID = item["id"]?.jsonPrimitive?.content ?: return@forEach
            val name = item["name"]?.jsonPrimitive?.content ?: ""
            val duration = item["duration"]?.jsonPrimitive?.long ?: 0

            val artists = item["artists"]?.jsonArray?.map {
                it.jsonObject["name"]?.jsonPrimitive?.content ?: ""
            } ?: emptyList()

            val album = item["album"]?.jsonObject
            val albumName = album?.get("name")?.jsonPrimitive?.content ?: ""
            val urlCover = album?.get("url_cover")?.jsonObject
            val coverDomain = urlCover?.get("urls")?.jsonArray?.firstOrNull()?.jsonPrimitive?.content ?: ""
            val coverUri = urlCover?.get("uri")?.jsonPrimitive?.content ?: ""
            val cover = if (coverDomain.isNotEmpty() && coverUri.isNotEmpty()) {
                "$coverDomain$coverUri~c5_375x375.jpg"
            } else ""

            val bitRates = item["bit_rates"]?.jsonArray
            val displaySize = bitRates?.maxByOrNull {
                it.jsonObject["size"]?.jsonPrimitive?.long ?: 0
            }?.jsonObject?.get("size")?.jsonPrimitive?.long ?: 0

            val seconds = duration / 1000
            val bitrate = if (seconds > 0 && displaySize > 0) (displaySize * 8 / 1000 / seconds).toInt() else 0

            songs.add(
                Song(
                    id = trackID,
                    name = name,
                    artist = artists.joinToString("、"),
                    album = albumName,
                    duration = seconds,
                    size = displaySize,
                    bitrate = bitrate.toLong(),
                    source = "soda",
                    cover = cover,
                    link = "https://www.qishui.com/track/$trackID",
                    extra = mapOf("track_id" to trackID)
                )
            )
        }

        return songs
    }

    override suspend fun searchPlaylist(keyword: String): List<Playlist> {
        val response = client.get("https://api.qishui.com/luna/pc/search/playlist") {
            sodaHeaders()
            url {
                parameters.append("q", keyword)
                parameters.append("cursor", "0")
                parameters.append("search_method", "input")
                parameters.append("aid", "386088")
                parameters.append("device_platform", "web")
                parameters.append("channel", "pc_web")
            }
        }

        val json = response.body<JsonObject>()
        val playlists = mutableListOf<Playlist>()

        val resultGroups = json["result_groups"]?.jsonArray
        if (resultGroups.isNullOrEmpty() || resultGroups[0].jsonObject["data"].let { it == null || it.jsonArray.isEmpty() }) {
            return emptyList()
        }

        resultGroups[0].jsonObject["data"]?.jsonArray?.forEach { itemEl ->
            val pl = itemEl.jsonObject["entity"]?.jsonObject?.get("playlist")?.jsonObject ?: return@forEach

            val id = pl["id"]?.jsonPrimitive?.content ?: return@forEach
            val title = pl["title"]?.jsonPrimitive?.content ?: ""
            val desc = pl["desc"]?.jsonPrimitive?.content ?: ""
            val owner = pl["owner"]?.jsonObject
            val creator = owner?.get("public_name")?.jsonPrimitive?.content
                ?: owner?.get("nickname")?.jsonPrimitive?.content ?: ""
            val countTracks = pl["count_tracks"]?.jsonPrimitive?.long ?: 0

            val urlCover = pl["url_cover"]?.jsonObject
            val coverDomain = urlCover?.get("urls")?.jsonArray?.firstOrNull()?.jsonPrimitive?.content ?: ""
            val coverUri = urlCover?.get("uri")?.jsonPrimitive?.content ?: ""
            var cover = if (coverDomain.isNotEmpty()) {
                if (coverUri.isNotEmpty() && !coverDomain.contains(coverUri)) {
                    "$coverDomain$coverUri"
                } else coverDomain
            } else ""
            if (cover.isNotEmpty() && !cover.contains("~")) {
                cover += "~c5_300x300.jpg"
            }

            playlists.add(
                Playlist(
                    id = id,
                    name = title,
                    cover = cover,
                    trackCount = countTracks,
                    creator = creator,
                    description = desc,
                    source = "soda",
                    link = "https://www.qishui.com/playlist/$id"
                )
            )
        }

        return playlists
    }

    override suspend fun getPlaylistSongs(id: String): List<Song> {
        val (_, songs) = fetchPlaylistDetail(id)
        return songs
    }

    override suspend fun parsePlaylist(link: String): PlaylistDetail {
        val regex = "playlist/(\\d+)".toRegex()
        val match = regex.find(link) ?: throw IllegalArgumentException("Invalid soda playlist link")
        val playlistID = match.groupValues[1]
        return fetchPlaylistDetail(playlistID)
    }

    private suspend fun fetchPlaylistDetail(id: String): PlaylistDetail {
        val response = client.get("https://api.qishui.com/luna/pc/playlist/detail") {
            sodaHeaders()
            url {
                parameters.append("playlist_id", id)
                parameters.append("cursor", "0")
                parameters.append("cnt", "20")
                parameters.append("aid", "386088")
                parameters.append("device_platform", "web")
                parameters.append("channel", "pc_web")
            }
        }

        val json = response.body<JsonObject>()

        val pl = json["playlist"]?.jsonObject ?: throw Exception("Playlist not found")
        val plTitle = pl["title"]?.jsonPrimitive?.content ?: ""
        val plDesc = pl["desc"]?.jsonPrimitive?.content ?: ""
        val owner = pl["owner"]?.jsonObject
        val plCreator = owner?.get("nickname")?.jsonPrimitive?.content ?: ""
        val plCountTracks = pl["count_tracks"]?.jsonPrimitive?.long ?: 0

        val urlCover = pl["url_cover"]?.jsonObject
        val coverDomain = urlCover?.get("urls")?.jsonArray?.firstOrNull()?.jsonPrimitive?.content ?: ""
        val coverUri = urlCover?.get("uri")?.jsonPrimitive?.content ?: ""
        var plCover = if (coverDomain.isNotEmpty()) {
            if (coverUri.isNotEmpty() && !coverDomain.contains(coverUri)) {
                "$coverDomain$coverUri"
            } else coverDomain
        } else ""
        if (plCover.isNotEmpty() && !plCover.contains("~")) {
            plCover += "~c5_300x300.jpg"
        }

        val playlist = Playlist(
            id = id,
            name = plTitle,
            cover = plCover,
            creator = plCreator,
            description = plDesc,
            trackCount = plCountTracks,
            source = "soda",
            link = "https://www.qishui.com/playlist/$id"
        )

        val songs = mutableListOf<Song>()

        json["media_resources"]?.jsonArray?.forEach { itemEl ->
            if (itemEl.jsonObject["type"]?.jsonPrimitive?.content != "track") return@forEach

            val track = itemEl.jsonObject["entity"]?.jsonObject?.get("track_wrapper")
                ?.jsonObject?.get("track")?.jsonObject ?: return@forEach

            val trackID = track["id"]?.jsonPrimitive?.content ?: return@forEach
            val name = track["name"]?.jsonPrimitive?.content ?: ""
            val duration = track["duration"]?.jsonPrimitive?.long ?: 0

            val artists = track["artists"]?.jsonArray?.map {
                it.jsonObject["name"]?.jsonPrimitive?.content ?: ""
            } ?: emptyList()

            val album = track["album"]?.jsonObject
            val albumName = album?.get("name")?.jsonPrimitive?.content ?: ""
            val urlCover2 = album?.get("url_cover")?.jsonObject
            val coverDomain2 = urlCover2?.get("urls")?.jsonArray?.firstOrNull()?.jsonPrimitive?.content ?: ""
            val coverUri2 = urlCover2?.get("uri")?.jsonPrimitive?.content ?: ""
            val cover = if (coverDomain2.isNotEmpty() && coverUri2.isNotEmpty()) {
                "$coverDomain2$coverUri2~c5_375x375.jpg"
            } else ""

            var displaySize: Long = 0
            track["bit_rates"]?.jsonArray?.forEach {
                val size = it.jsonObject["size"]?.jsonPrimitive?.long ?: 0
                if (size > displaySize) displaySize = size
            }
            track["audio_info"]?.jsonObject?.get("play_info_list")?.jsonArray?.forEach {
                val size = it.jsonObject["size"]?.jsonPrimitive?.long ?: 0
                if (size > displaySize) displaySize = size
            }

            val seconds = duration / 1000
            val bitrate = if (seconds > 0 && displaySize > 0) (displaySize * 8 / 1000 / seconds).toInt() else 0

            val playInfoList = track["audio_info"]?.jsonObject?.get("play_info_list")?.jsonArray
            val best = playInfoList?.maxByOrNull {
                it.jsonObject["size"]?.jsonPrimitive?.long ?: 0
            }?.jsonObject

            val song = Song(
                id = trackID,
                name = name,
                artist = artists.joinToString("、"),
                album = albumName,
                duration = seconds,
                size = displaySize,
                bitrate = bitrate.toLong(),
                source = "soda",
                cover = cover,
                link = "https://www.qishui.com/track/$trackID",
                extra = mapOf("track_id" to trackID)
            )

            if (best != null) {
                val mainPlayUrl = best["main_play_url"]?.jsonPrimitive?.content ?: ""
                val playAuth = best["play_auth"]?.jsonPrimitive?.content ?: ""
                val bestSize = best["size"]?.jsonPrimitive?.long ?: 0
                val bestBitrate = best["bitrate"]?.jsonPrimitive?.long ?: 0
                val format = best["format"]?.jsonPrimitive?.content ?: ""

                if (mainPlayUrl.isNotEmpty()) {
                    songs.add(
                        song.copy(
                            url = "$mainPlayUrl#auth=${URLEncoder.encode(playAuth, "UTF-8")}",
                            size = if (song.size == 0L) bestSize else song.size,
                            ext = format,
                            bitrate = bestBitrate
                        )
                    )
                    return@forEach
                }
            }

            songs.add(song)
        }

        return PlaylistDetail(playlist, songs)
    }

    override suspend fun parse(link: String): Song {
        val regex = "track/(\\d+)".toRegex()
        val match = regex.find(link) ?: throw IllegalArgumentException("Invalid soda link")
        val trackID = match.groupValues[1]
        return fetchSongDetail(trackID)
    }

    override suspend fun getDownloadURL(song: Song): String {
        if (song.url.contains("#auth=")) {
            return song.url.split("#auth=")[0]
        }
        if (song.source != "soda") throw IllegalArgumentException("Source mismatch")
        val trackID = song.extra["track_id"] ?: song.id
        val info = getDownloadInfo(song)
        return info.url
    }

    data class DownloadInfo(val url: String, val playAuth: String, val format: String, val size: Long)

    suspend fun getDownloadInfo(song: Song): DownloadInfo {
        if (song.url.contains("#auth=")) {
            val parts = song.url.split("#auth=")
            if (parts.size == 2) {
                val auth = URLDecoder.decode(parts[1], "UTF-8")
                return DownloadInfo(parts[0], auth, song.ext, song.size)
            }
        }

        if (song.source != "soda") throw IllegalArgumentException("Source mismatch")
        val trackID = song.extra["track_id"] ?: song.id

        val v2Response = client.get("https://api.qishui.com/luna/pc/track_v2") {
            sodaHeaders()
            parameter("track_id", trackID)
            parameter("media_type", "track")
            parameter("aid", "386088")
            parameter("device_platform", "web")
            parameter("channel", "pc_web")
        }

        val v2Json = v2Response.body<JsonObject>()
        val playerInfoUrl = v2Json["track_player"]?.jsonObject?.get("url_player_info")?.jsonPrimitive?.content
            ?: throw Exception("Player info URL not found")

        return fetchPlayerInfo(playerInfoUrl)
    }

    private suspend fun fetchPlayerInfo(playerInfoURL: String): DownloadInfo {
        val response = client.get(playerInfoURL) { sodaHeaders() }
        val json = response.body<JsonObject>()

        val playInfoList = json["Result"]?.jsonObject?.get("Data")
            ?.jsonObject?.get("PlayInfoList")?.jsonArray
            ?: throw Exception("No play info list")

        if (playInfoList.isEmpty()) throw Exception("No audio stream found")

        val sorted = playInfoList.sortedByDescending {
            it.jsonObject["Size"]?.jsonPrimitive?.long ?: 0
        }

        val best = sorted.first().jsonObject
        val mainUrl = best["MainPlayUrl"]?.jsonPrimitive?.content ?: ""
        val backupUrl = best["BackupPlayUrl"]?.jsonPrimitive?.content ?: ""
        val downloadURL = if (mainUrl.isNotEmpty()) mainUrl else backupUrl

        if (downloadURL.isEmpty()) throw Exception("Invalid download URL")

        return DownloadInfo(
            url = downloadURL,
            playAuth = best["PlayAuth"]?.jsonPrimitive?.content ?: "",
            format = best["Format"]?.jsonPrimitive?.content ?: "",
            size = best["Size"]?.jsonPrimitive?.long ?: 0
        )
    }

    private suspend fun fetchSongDetail(trackID: String): Song {
        val v2Response = client.get("https://api.qishui.com/luna/pc/track_v2") {
            sodaHeaders()
            parameter("track_id", trackID)
            parameter("media_type", "track")
            parameter("aid", "386088")
            parameter("device_platform", "web")
            parameter("channel", "pc_web")
        }

        val v2Json = v2Response.body<JsonObject>()
        val info = v2Json["track_info"]?.jsonObject ?: throw Exception("Track info not found")

        val id = info["id"]?.jsonPrimitive?.content ?: throw Exception("Track ID not found")
        val name = info["name"]?.jsonPrimitive?.content ?: ""
        val duration = info["duration"]?.jsonPrimitive?.long ?: 0

        val artists = info["artists"]?.jsonArray?.map {
            it.jsonObject["name"]?.jsonPrimitive?.content ?: ""
        } ?: emptyList()

        val album = info["album"]?.jsonObject
        val albumName = album?.get("name")?.jsonPrimitive?.content ?: ""
        val urlCover = album?.get("url_cover")?.jsonObject
        val coverDomain = urlCover?.get("urls")?.jsonArray?.firstOrNull()?.jsonPrimitive?.content ?: ""
        val coverUri = urlCover?.get("uri")?.jsonPrimitive?.content ?: ""
        val cover = if (coverDomain.isNotEmpty() && coverUri.isNotEmpty()) {
            "$coverDomain$coverUri~c5_375x375.jpg"
        } else ""

        val song = Song(
            id = id,
            name = name,
            artist = artists.joinToString("、"),
            album = albumName,
            duration = duration / 1000,
            source = "soda",
            cover = cover,
            link = "https://www.qishui.com/track/$id",
            extra = mapOf("track_id" to id)
        )

        val playerInfoUrl = v2Json["track_player"]?.jsonObject?.get("url_player_info")?.jsonPrimitive?.content
        if (playerInfoUrl != null) {
            try {
                val dInfo = fetchPlayerInfo(playerInfoUrl)
                return song.copy(
                    url = "${dInfo.url}#auth=${URLEncoder.encode(dInfo.playAuth, "UTF-8")}",
                    size = dInfo.size,
                    ext = dInfo.format,
                    bitrate = (if (song.duration > 0) (dInfo.size * 8 / 1000 / song.duration).toInt() else 0).toLong()
                )
            } catch (_: Exception) {
            }
        }

        return song
    }

    override suspend fun getLyrics(song: Song): String {
        if (song.source != "soda") throw IllegalArgumentException("Source mismatch")
        val trackID = song.extra["track_id"] ?: song.id

        val response = client.get("https://api.qishui.com/luna/pc/track_v2") {
            sodaHeaders()
            parameter("track_id", trackID)
            parameter("media_type", "track")
            parameter("aid", "386088")
            parameter("device_platform", "web")
            parameter("channel", "pc_web")
        }

        val json = response.body<JsonObject>()
        val content = json["lyric"]?.jsonObject?.get("content")?.jsonPrimitive?.content
            ?: return ""

        return parseSodaLyric(content)
    }

    private fun parseSodaLyric(raw: String): String {
        val sb = StringBuilder()
        val lineRegex = "^\\[(\\d+),(\\d+)\\](.*)$".toRegex()
        val wordRegex = "<[^>]+>".toRegex()

        raw.lines().forEach { line ->
            val trimmed = line.trim()
            if (trimmed.isEmpty()) return@forEach

            val match = lineRegex.find(trimmed)
            if (match != null && match.groupValues.size >= 4) {
                val startTimeStr = match.groupValues[1]
                val content = match.groupValues[3]
                val cleanContent = wordRegex.replace(content, "")
                val startTime = startTimeStr.toIntOrNull() ?: 0
                val minutes = startTime / 60000
                val seconds = (startTime % 60000) / 1000
                val millis = (startTime % 1000) / 10
                sb.appendLine("[%02d:%02d.%02d]$cleanContent".format(minutes, seconds, millis))
            }
        }

        return sb.toString()
    }

    suspend fun download(song: Song): ByteArray {
        val info = getDownloadInfo(song)
        val response = client.get(info.url) { sodaHeaders() }
        val encryptedData = response.body<ByteArray>()
        return SodaCrypto.decryptAudio(encryptedData, info.playAuth)
    }
}
