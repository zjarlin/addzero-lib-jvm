package site.addzero.network.call.musiclib.api.joox

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
import java.util.Base64

/**
 * Joox 音乐 API 实现
 */
class JooxAPI(
    override var cookie: String = DEFAULT_COOKIE,
    private val client: HttpClient = HttpClientManager.client,
) : CookieMusicProvider {

    override val name: String = "Joox"
    override val source: String = "joox"

    companion object {
        const val USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36"
        const val DEFAULT_COOKIE = "wmid=142420656; user_type=1; country=id; session_key=2a5d97d05dc8fe238150184eaf3519ad;"
        const val X_FORWARDED_FOR = "36.73.34.109"
    }

    private fun HttpRequestBuilder.jooxHeaders() {
        header(HttpHeaders.UserAgent, USER_AGENT)
        header(HttpHeaders.Cookie, cookie)
        header("X-Forwarded-For", X_FORWARDED_FOR)
    }

    override suspend fun search(keyword: String): List<Song> {
        val response = client.get("https://cache.api.joox.com/openjoox/v3/search") {
            jooxHeaders()
            parameter("country", "sg")
            parameter("lang", "zh_cn")
            parameter("keyword", keyword)
        }

        val json = response.body<JsonObject>()
        val songs = mutableListOf<Song>()

        json["section_list"]?.jsonArray?.forEach { sectionEl ->
            val section = sectionEl.jsonObject
            section["item_list"]?.jsonArray?.forEach { itemsEl ->
                val items = itemsEl.jsonObject
                items["song"]?.jsonArray?.forEach { songEl ->
                    val songItem = songEl.jsonObject
                    val info = songItem["song_info"]?.jsonObject ?: return@forEach

                    val id = info["id"]?.jsonPrimitive?.content ?: return@forEach
                    val name = info["name"]?.jsonPrimitive?.content ?: ""
                    val albumName = info["album_name"]?.jsonPrimitive?.content ?: ""
                    val playDuration = info["play_duration"]?.jsonPrimitive?.long ?: 0

                    val artists = info["artist_list"]?.jsonArray?.map {
                        it.jsonObject["name"]?.jsonPrimitive?.content ?: ""
                    } ?: emptyList()

                    val images = info["images"]?.jsonArray
                    var cover = ""
                    images?.forEach { imgEl ->
                        val img = imgEl.jsonObject
                        if (img["width"]?.jsonPrimitive?.long == 300.toLong()) {
                            cover = img["url"]?.jsonPrimitive?.content ?: ""
                        }
                    }
                    if (cover.isEmpty() && !images.isNullOrEmpty()) {
                        cover = images[0].jsonObject["url"]?.jsonPrimitive?.content ?: ""
                    }

                    songs.add(Song(
                        id = id,
                        name = name,
                        artist = artists.joinToString("、"),
                        album = albumName,
                        duration = playDuration,
                        source = "joox",
                        cover = cover,
                        link = "https://www.joox.com/hk/single/$id",
                        extra = mapOf("songid" to id)
                    ))
                }
            }
        }

        return songs
    }

    override suspend fun searchPlaylist(keyword: String): List<Playlist> {
        val response = client.get("https://cache.api.joox.com/openjoox/v3/search") {
            jooxHeaders()
            parameter("country", "sg")
            parameter("lang", "zh_cn")
            parameter("keyword", keyword)
        }

        val json = response.body<JsonObject>()
        val playlists = mutableListOf<Playlist>()

        json["section_list"]?.jsonArray?.forEach { sectionEl ->
            val section = sectionEl.jsonObject
            section["item_list"]?.jsonArray?.forEach { itemsEl ->
                val items = itemsEl.jsonObject
                val type = items["type"]?.jsonPrimitive?.long ?: 0
                if (type != 1.toLong()) return@forEach

                val info = items["editor_playlist"]?.jsonObject ?: return@forEach
                val id = info["id"]?.jsonPrimitive?.content ?: return@forEach
                val name = info["name"]?.jsonPrimitive?.content ?: ""

                val images = info["images"]?.jsonArray
                var cover = ""
                images?.forEach { imgEl ->
                    val img = imgEl.jsonObject
                    if (img["width"]?.jsonPrimitive?.long == 300.toLong()) {
                        cover = img["url"]?.jsonPrimitive?.content ?: ""
                    }
                }
                if (cover.isEmpty() && !images.isNullOrEmpty()) {
                    cover = images[0].jsonObject["url"]?.jsonPrimitive?.content ?: ""
                }

                playlists.add(Playlist(
                    id = id,
                    name = name,
                    cover = cover,
                    source = "joox",
                    link = "https://www.joox.com/hk/playlist/$id",
                    extra = mapOf("playlist_id" to id)
                ))
            }
        }

        return playlists
    }

    override suspend fun getPlaylistSongs(id: String): List<Song> {
        val response = client.get("https://cache.api.joox.com/openjoox/v3/playlist") {
            jooxHeaders()
            parameter("id", id)
            parameter("country", "sg")
            parameter("lang", "zh_cn")
        }

        val json = response.body<JsonObject>()
        val songs = mutableListOf<Song>()

        json["section_list"]?.jsonArray?.forEach { sectionEl ->
            val section = sectionEl.jsonObject
            section["item_list"]?.jsonArray?.forEach { itemsEl ->
                val items = itemsEl.jsonObject
                val type = items["type"]?.jsonPrimitive?.long ?: 0
                if (type != 5.toLong()) return@forEach

                items["song"]?.jsonArray?.forEach { songEl ->
                    val songItem = songEl.jsonObject
                    val info = songItem["song_info"]?.jsonObject ?: return@forEach

                    val id = info["id"]?.jsonPrimitive?.content ?: return@forEach
                    val name = info["name"]?.jsonPrimitive?.content ?: ""
                    val albumName = info["album_name"]?.jsonPrimitive?.content ?: ""
                    val albumID = info["album_id"]?.jsonPrimitive?.content ?: ""
                    val playDuration = info["play_duration"]?.jsonPrimitive?.long ?: 0

                    val artists = info["artist_list"]?.jsonArray?.map {
                        it.jsonObject["name"]?.jsonPrimitive?.content ?: ""
                    } ?: emptyList()

                    val images = info["images"]?.jsonArray
                    var cover = ""
                    images?.forEach { imgEl ->
                        val img = imgEl.jsonObject
                        if (img["width"]?.jsonPrimitive?.long == 300.toLong()) {
                            cover = img["url"]?.jsonPrimitive?.content ?: ""
                        }
                    }
                    if (cover.isEmpty() && !images.isNullOrEmpty()) {
                        cover = images[0].jsonObject["url"]?.jsonPrimitive?.content ?: ""
                    }

                    if (cover.isEmpty() && albumID.isNotEmpty()) {
                        cover = "https://imgcache.joox.com/music/joox/photo/mid_album_300/${albumID.takeLast(2)}/${albumID.takeLast(1)}/$albumID.jpg"
                    }

                    songs.add(Song(
                        id = id,
                        name = name,
                        artist = artists.joinToString("、"),
                        album = albumName,
                        duration = playDuration,
                        source = "joox",
                        cover = cover,
                        link = "https://www.joox.com/hk/single/$id",
                        extra = mapOf("songid" to id)
                    ))
                }
            }
        }

        return songs
    }

    override suspend fun parsePlaylist(link: String): PlaylistDetail {
        throw NotImplementedError("Joox playlist parsing not implemented")
    }

    override suspend fun parse(link: String): Song {
        val regex = "joox\\.com/.*/single/([a-zA-Z0-9]+)".toRegex()
        val match = regex.find(link)
        val songID = if (match != null) {
            match.groupValues[1]
        } else if (link.length > 10 && !link.contains("/")) {
            link
        } else {
            throw IllegalArgumentException("Invalid joox link")
        }

        return fetchSongInfo(songID)
    }

    override suspend fun getDownloadURL(song: Song): String {
        if (song.source != "joox") throw IllegalArgumentException("Source mismatch")
        val songID = song.extra["songid"] ?: song.id
        val info = fetchSongInfo(songID)
        return info.url
    }

    private suspend fun fetchSongInfo(songID: String): Song {
        val response = client.get("https://api.joox.com/web-fcgi-bin/web_get_songinfo") {
            jooxHeaders()
            parameter("songid", songID)
            parameter("lang", "zh_cn")
            parameter("country", "sg")
        }

        var body = response.body<String>()
        if (body.startsWith("MusicInfoCallback(")) {
            body = body.removePrefix("MusicInfoCallback(").removeSuffix(")")
        }

        val json = Json.parseToJsonElement(body).jsonObject
        val msong = json["msong"]?.jsonPrimitive?.content ?: ""
        val msinger = json["msinger"]?.jsonPrimitive?.content ?: ""
        val malbum = json["malbum"]?.jsonPrimitive?.content ?: ""
        val img = json["img"]?.jsonPrimitive?.content ?: ""
        val mInterval = json["minterval"]?.jsonPrimitive?.long ?: 0
        val r320Url = json["r320Url"]?.jsonPrimitive?.content ?: ""
        val r192Url = json["r192Url"]?.jsonPrimitive?.content ?: ""
        val mp3Url = json["mp3Url"]?.jsonPrimitive?.content ?: ""
        val m4aUrl = json["m4aUrl"]?.jsonPrimitive?.content ?: ""

        val downloadURL = listOf(r320Url, r192Url, mp3Url, m4aUrl).firstOrNull { it.isNotEmpty() }
            ?: throw Exception("No valid download URL found")

        return Song(
            id = songID,
            name = msong,
            artist = msinger,
            album = malbum,
            duration = mInterval,
            source = "joox",
            cover = img,
            url = downloadURL,
            link = "https://www.joox.com/hk/single/$songID",
            extra = mapOf("songid" to songID)
        )
    }

    override suspend fun getLyrics(song: Song): String {
        if (song.source != "joox") throw IllegalArgumentException("Source mismatch")
        val songID = song.extra["songid"] ?: song.id

        val response = client.get("https://api.joox.com/web-fcgi-bin/web_lyric") {
            jooxHeaders()
            parameter("musicid", songID)
            parameter("country", "sg")
            parameter("lang", "zh_cn")
        }

        var body = response.body<String>()
        if (body.contains("MusicJsonCallback(")) {
            val start = body.indexOf("MusicJsonCallback(")
            body = body.substring(start + "MusicJsonCallback(".length).removeSuffix(")")
        }

        val json = Json.parseToJsonElement(body).jsonObject
        val lyric = json["lyric"]?.jsonPrimitive?.content
            ?: throw Exception("Lyric not found")

        return String(Base64.getDecoder().decode(lyric))
    }
}
