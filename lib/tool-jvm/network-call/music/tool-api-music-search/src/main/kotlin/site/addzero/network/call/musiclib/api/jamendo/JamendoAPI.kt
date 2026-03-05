package site.addzero.network.call.musiclib.api.jamendo

import site.addzero.network.call.musiclib.crypto.CommonCrypto
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

/**
 * Jamendo API 实现
 */
class JamendoAPI(
    override var cookie: String = "",
    private val client: HttpClient = HttpClientManager.client,
) : CookieMusicProvider {

    override val name: String = "Jamendo"
    override val source: String = "jamendo"

    companion object {
        const val USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36"
        const val REFERER = "https://www.jamendo.com/search?q=musicdl"
        const val X_JAM_VERSION = "4gvfvv"
        const val SEARCH_API = "https://www.jamendo.com/api/search"
        const val SEARCH_API_PATH = "/api/search"
        const val TRACK_API = "https://www.jamendo.com/api/tracks"
        const val TRACK_API_PATH = "/api/tracks"
        const val PLAYLIST_API = "https://www.jamendo.com/api/playlists"
        const val PLAYLIST_API_PATH = "/api/playlists"
        const val PLAYLIST_TRACKS_API = "https://www.jamendo.com/api/playlists/tracks"
        const val PLAYLIST_TRACKS_PATH = "/api/playlists/tracks"
        const val CLIENT_ID = "9873ff31"
    }

    private fun HttpRequestBuilder.jamendoHeaders(xJamCall: String) {
        header(HttpHeaders.UserAgent, USER_AGENT)
        header(HttpHeaders.Referrer, REFERER)
        header("x-jam-call", xJamCall)
        header("x-jam-version", X_JAM_VERSION)
        header("x-requested-with", "XMLHttpRequest")
        header(HttpHeaders.Cookie, cookie)
    }

    private fun makeXJamCall(path: String): String {
        val r = Math.random()
        val randStr = r.toString()
        val data = path + randStr
        val digest = CommonCrypto.sha1(data)
        return "$$digest*$randStr~"
    }

    override suspend fun search(keyword: String): List<Song> {
        val xJamCall = makeXJamCall(SEARCH_API_PATH)

        val response = client.get(SEARCH_API) {
            jamendoHeaders(xJamCall)
            parameter("query", keyword)
            parameter("type", "track")
            parameter("limit", "20")
            parameter("identities", "www")
        }

        val json = response.body<JsonArray>()
        val songs = mutableListOf<Song>()

        json.forEach { itemEl ->
            val item = itemEl.jsonObject
            val id = item["id"]?.jsonPrimitive?.long ?: return@forEach
            val name = item["name"]?.jsonPrimitive?.content ?: ""
            val duration = item["duration"]?.jsonPrimitive?.long ?: 0

            val artist = item["artist"]?.jsonObject?.get("name")?.jsonPrimitive?.content ?: ""
            val album = item["album"]?.jsonObject?.get("name")?.jsonPrimitive?.content ?: ""
            val cover = item["cover"]?.jsonObject?.get("big")?.jsonObject?.get("size300")?.jsonPrimitive?.content ?: ""

            val download = item["download"]?.jsonObject
            val stream = item["stream"]?.jsonObject

            val (downloadURL, ext) = pickBestQuality(download, stream)
            if (downloadURL.isEmpty()) return@forEach

            songs.add(Song(
                id = id.toString(),
                name = name,
                artist = artist,
                album = album,
                duration = duration,
                ext = ext,
                source = "jamendo",
                cover = cover,
                url = downloadURL,
                link = "https://www.jamendo.com/track/$id",
                extra = mapOf("track_id" to id.toString())
            ))
        }

        return songs
    }

    override suspend fun searchPlaylist(keyword: String): List<Playlist> {
        val xJamCall = makeXJamCall(SEARCH_API_PATH)

        val response = client.get(SEARCH_API) {
            jamendoHeaders(xJamCall)
            parameter("query", keyword)
            parameter("type", "playlist")
            parameter("limit", "20")
            parameter("identities", "www")
        }

        val json = response.body<JsonArray>()
        val playlists = mutableListOf<Playlist>()

        json.forEach { itemEl ->
            val item = itemEl.jsonObject
            val id = item["id"]?.jsonPrimitive?.long ?: return@forEach
            val name = item["name"]?.jsonPrimitive?.content ?: ""
            val userName = item["user_name"]?.jsonPrimitive?.content ?: ""
            val image = item["image"]?.jsonPrimitive?.content ?: ""

            playlists.add(Playlist(
                id = id.toString(),
                name = name,
                creator = userName,
                cover = image,
                source = "jamendo",
                link = "https://www.jamendo.com/playlist/$id"
            ))
        }

        return playlists
    }

    override suspend fun getPlaylistSongs(id: String): List<Song> {
        val xJamCall = makeXJamCall(PLAYLIST_TRACKS_PATH)

        val response = client.get(PLAYLIST_TRACKS_API) {
            jamendoHeaders(xJamCall)
            parameter("id", id)
        }

        val json = response.body<JsonArray>()
        val songs = mutableListOf<Song>()

        json.forEach { itemEl ->
            val item = itemEl.jsonObject
            val id = item["id"]?.jsonPrimitive?.long ?: return@forEach
            val name = item["name"]?.jsonPrimitive?.content ?: ""
            val duration = item["duration"]?.jsonPrimitive?.long ?: 0

            val artist = item["artist"]?.jsonObject?.get("name")?.jsonPrimitive?.content ?: ""
            val album = item["album"]?.jsonObject?.get("name")?.jsonPrimitive?.content ?: ""
            val cover = item["cover"]?.jsonObject?.get("big")?.jsonObject?.get("size300")?.jsonPrimitive?.content ?: ""

            val download = item["download"]?.jsonObject
            val stream = item["stream"]?.jsonObject

            val (downloadURL, ext) = pickBestQuality(download, stream)
            if (downloadURL.isEmpty()) return@forEach

            songs.add(Song(
                id = id.toString(),
                name = name,
                artist = artist,
                album = album,
                duration = duration,
                ext = ext,
                source = "jamendo",
                cover = cover,
                url = downloadURL,
                link = "https://www.jamendo.com/track/$id",
                extra = mapOf("track_id" to id.toString())
            ))
        }

        return songs
    }

    override suspend fun parsePlaylist(link: String): PlaylistDetail {
        throw NotImplementedError("Jamendo playlist parsing not implemented")
    }

    override suspend fun parse(link: String): Song {
        val regex = "jamendo\\.com/track/(\\d+)".toRegex()
        val match = regex.find(link) ?: throw IllegalArgumentException("Invalid jamendo link")
        val trackID = match.groupValues[1]
        return getTrackByID(trackID)
    }

    override suspend fun getDownloadURL(song: Song): String {
        if (song.source != "jamendo") throw IllegalArgumentException("Source mismatch")
        if (song.url.isNotEmpty()) return song.url

        val trackID = song.extra["track_id"] ?: song.id
        val info = getTrackByID(trackID)
        return info.url
    }

    private suspend fun getTrackByID(id: String): Song {
        val xJamCall = makeXJamCall(TRACK_API_PATH)

        val response = client.get(TRACK_API) {
            jamendoHeaders(xJamCall)
            parameter("id", id)
        }

        val json = response.body<JsonArray>()
        if (json.isEmpty()) throw Exception("Track not found")

        val item = json[0].jsonObject
        val trackId = item["id"]?.jsonPrimitive?.long ?: throw Exception("Track ID not found")
        val name = item["name"]?.jsonPrimitive?.content ?: ""
        val duration = item["duration"]?.jsonPrimitive?.long ?: 0

        val artist = item["artist"]?.jsonObject?.get("name")?.jsonPrimitive?.content ?: ""
        val album = item["album"]?.jsonObject?.get("name")?.jsonPrimitive?.content ?: ""
        val cover = item["cover"]?.jsonObject?.get("big")?.jsonObject?.get("size300")?.jsonPrimitive?.content ?: ""

        val download = item["download"]?.jsonObject
        val stream = item["stream"]?.jsonObject

        val (downloadURL, ext) = pickBestQuality(download, stream)
        if (downloadURL.isEmpty()) throw Exception("No valid stream found")

        return Song(
            id = trackId.toString(),
            name = name,
            artist = artist,
            album = album,
            duration = duration,
            ext = ext,
            source = "jamendo",
            cover = cover,
            url = downloadURL,
            link = "https://www.jamendo.com/track/$trackId",
            extra = mapOf("track_id" to trackId.toString())
        )
    }

    override suspend fun getLyrics(song: Song): String {
        return ""  // Jamendo 没有歌词
    }

    private fun pickBestQuality(download: JsonObject?, stream: JsonObject?): Pair<String, String> {
        val streams = mutableMapOf<String, String>()

        download?.forEach { (key, value) ->
            if (value is JsonPrimitive && value.content.isNotEmpty()) {
                streams[key] = value.content
            }
        }

        if (streams.isEmpty()) {
            stream?.forEach { (key, value) ->
                if (value is JsonPrimitive && value.content.isNotEmpty()) {
                    streams[key] = value.content
                }
            }
        }

        return when {
            streams["flac"]?.isNotEmpty() == true -> Pair(streams["flac"]!!, "flac")
            streams["mp3"]?.isNotEmpty() == true -> Pair(streams["mp3"]!!, "mp3")
            streams["ogg"]?.isNotEmpty() == true -> Pair(streams["ogg"]!!, "ogg")
            else -> Pair("", "")
        }
    }
}
