package site.addzero.network.call.musiclib.api.kugou

import site.addzero.network.call.musiclib.model.Playlist
import site.addzero.network.call.musiclib.model.PlaylistDetail
import site.addzero.network.call.musiclib.model.Song
import site.addzero.network.call.musiclib.provider.CookieMusicProvider
import site.addzero.network.call.musiclib.utils.HttpClientManager
import site.addzero.network.call.musiclib.utils.mobileHeaders
import io.ktor.client.HttpClient
import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.serialization.json.*
import java.util.Base64

/**
 * 酷狗音乐 API 实现
 */
class KugouAPI(
    override var cookie: String = "",
    private val client: HttpClient = HttpClientManager.client,
) : CookieMusicProvider {

    override val name: String = "酷狗音乐"
    override val source: String = "kugou"

    companion object {
        const val MOBILE_USER_AGENT = "Mozilla/5.0 (iPhone; CPU iPhone OS 13_2_3 like Mac OS X) AppleWebKit/605.1.15"
        const val MOBILE_REFERER = "http://m.kugou.com"
    }

    override suspend fun search(keyword: String): List<Song> {
        val response = client.get("http://songsearch.kugou.com/song_search_v2") {
            mobileHeaders(null, null)
            url {
                parameters.append("keyword", keyword)
                parameters.append("platform", "WebFilter")
                parameters.append("format", "json")
                parameters.append("page", "1")
                parameters.append("pagesize", "10")
            }
        }

        val json = response.body<JsonObject>()
        val songs = mutableListOf<Song>()

        json["data"]?.jsonObject?.get("lists")?.jsonArray?.forEach { itemEl ->
            val item = itemEl.jsonObject

            val privilege = item["Privilege"]?.jsonPrimitive?.long ?: 0
            if (privilege == 10.toLong()) return@forEach

            val fileHash = item["FileHash"]?.jsonPrimitive?.content ?: ""
            val sqFileHash = item["SQFileHash"]?.jsonPrimitive?.content ?: ""
            val hqFileHash = item["HQFileHash"]?.jsonPrimitive?.content ?: ""

            if (fileHash.isEmpty() && sqFileHash.isEmpty() && hqFileHash.isEmpty()) return@forEach

            val finalHash = when {
                isValidHash(sqFileHash) -> sqFileHash
                isValidHash(hqFileHash) -> hqFileHash
                else -> fileHash
            }

            val songName = item["SongName"]?.jsonPrimitive?.content ?: ""
            val singerName = item["SingerName"]?.jsonPrimitive?.content ?: ""
            val albumName = item["AlbumName"]?.jsonPrimitive?.content ?: ""
            val duration = item["Duration"]?.jsonPrimitive?.long ?: 0
            val image = item["Image"]?.jsonPrimitive?.content ?: ""

            val fileSize = when (val fs = item["FileSize"]) {
                is JsonPrimitive -> fs.longOrNull ?: 0
                else -> 0
            }

            val bitrate = if (duration > 0 && fileSize > 0) (fileSize * 8 / 1000 / duration).toInt() else 0
            val coverURL = image.replace("{size}", "240")

            songs.add(Song(
                id = finalHash,
                name = songName,
                artist = singerName,
                album = albumName,
                duration = duration,
                size = fileSize,
                bitrate = bitrate.toLong(),
                source = "kugou",
                cover = coverURL,
                link = "https://www.kugou.com/song/#hash=$finalHash",
                extra = mapOf("hash" to finalHash)
            ))
        }

        return songs
    }

    override suspend fun searchPlaylist(keyword: String): List<Playlist> {
        val response = client.get("http://mobilecdn.kugou.com/api/v3/search/special") {
            mobileHeaders(MOBILE_REFERER, cookie)
            url {
                parameters.append("keyword", keyword)
                parameters.append("platform", "WebFilter")
                parameters.append("format", "json")
                parameters.append("page", "1")
                parameters.append("pagesize", "10")
                parameters.append("filter", "0")
            }
        }

        val json = response.body<JsonObject>()
        val playlists = mutableListOf<Playlist>()

        json["data"]?.jsonObject?.get("info")?.jsonArray?.forEach { itemEl ->
            val item = itemEl.jsonObject
            val specialID = item["specialid"]?.jsonPrimitive?.long ?: return@forEach
            val specialName = item["specialname"]?.jsonPrimitive?.content ?: ""
            val intro = item["intro"]?.jsonPrimitive?.content ?: ""
            val imgURL = item["imgurl"]?.jsonPrimitive?.content ?: ""
            val songCount = item["songcount"]?.jsonPrimitive?.long ?: 0
            val playCount = item["playcount"]?.jsonPrimitive?.long ?: 0
            val nickName = item["nickname"]?.jsonPrimitive?.content ?: ""

            val cover = imgURL.replace("{size}", "240")

            playlists.add(Playlist(
                id = specialID.toString(),
                name = specialName,
                cover = cover,
                trackCount = songCount,
                playCount = playCount,
                creator = nickName,
                description = intro,
                source = "kugou",
                link = "https://www.kugou.com/yy/special/single/$specialID.html"
            ))
        }

        return playlists
    }

    override suspend fun getPlaylistSongs(id: String): List<Song> {
        val (_, songs) = fetchPlaylistDetail(id)
        return songs
    }

    override suspend fun parsePlaylist(link: String): PlaylistDetail {
        val regex = "special/single/(\\d+)\\.html".toRegex()
        val match = regex.find(link) ?: throw IllegalArgumentException("Invalid kugou playlist link")
        val specialID = match.groupValues[1]
        return fetchPlaylistDetail(specialID)
    }

    private suspend fun fetchPlaylistDetail(id: String): PlaylistDetail {
        val response = client.get("http://mobilecdn.kugou.com/api/v3/special/song") {
            mobileHeaders(MOBILE_REFERER, cookie)
            url {
                parameters.append("specialid", id)
                parameters.append("page", "1")
                parameters.append("pagesize", "300")
                parameters.append("version", "9108")
                parameters.append("area_code", "1")
            }
        }

        val json = response.body<JsonObject>()
        val info = json["data"]?.jsonObject?.get("info")?.jsonArray ?: emptyList()

        val playlist = Playlist(
            id = id,
            source = "kugou",
            link = "https://www.kugou.com/yy/special/single/$id.html"
        )

        val songs = mutableListOf<Song>()

        info.forEach { itemEl ->
            val item = itemEl.jsonObject
            val hash = item["hash"]?.jsonPrimitive?.content ?: ""
            val fileName = item["filename"]?.jsonPrimitive?.content ?: ""
            val duration = item["duration"]?.jsonPrimitive?.long ?: 0
            val fileSize = item["filesize"]?.jsonPrimitive?.long ?: 0
            val albumName = item["album_name"]?.jsonPrimitive?.content ?: ""
            val singerName = item["singername"]?.jsonPrimitive?.content ?: ""
            val songName = item["songname"]?.jsonPrimitive?.content ?: ""
            val remark = item["remark"]?.jsonPrimitive?.content ?: ""
            val unionCover = item["trans_param"]?.jsonObject?.get("union_cover")?.jsonPrimitive?.content ?: ""

            val name = if (songName.isNotEmpty()) songName else fileName.substringAfter(" - ", fileName)
            val artist = if (singerName.isNotEmpty()) singerName else fileName.substringBefore(" - ", "")
            val cover = if (unionCover.isNotEmpty()) unionCover.replace("{size}", "240") else ""
            val album = if (albumName.isNotEmpty()) albumName else remark

            songs.add(Song(
                id = hash,
                name = name,
                artist = artist,
                album = album,
                duration = duration,
                size = fileSize,
                source = "kugou",
                cover = cover,
                link = "https://www.kugou.com/song/#hash=$hash",
                extra = mapOf("hash" to hash)
            ))
        }

        return PlaylistDetail(playlist.copy(trackCount = songs.size.toLong()), songs)
    }

    override suspend fun parse(link: String): Song {
        val regex = "(?i)hash=([a-f0-9]{32})".toRegex()
        val match = regex.find(link) ?: throw IllegalArgumentException("Invalid kugou link")
        val hash = match.groupValues[1]
        return fetchSongInfo(hash)
    }

    override suspend fun getDownloadURL(song: Song): String {
        if (song.source != "kugou") throw IllegalArgumentException("Source mismatch")
        val hash = song.extra["hash"] ?: song.id
        val info = fetchSongInfo(hash)
        return info.url
    }

    private suspend fun fetchSongInfo(hash: String): Song {
        val response = client.get("http://m.kugou.com/app/i/getSongInfo.php") {
            mobileHeaders(MOBILE_REFERER, cookie)
            url {
                parameters.append("cmd", "playInfo")
                parameters.append("hash", hash)
            }
        }

        val json = try {
            response.body<JsonObject>()
        } catch (e: Exception) {
            return fallbackFetchSongInfo(hash)
        }

        val errcode = json["errcode"]?.jsonPrimitive?.long ?: 0
        if (errcode != 0.toLong() || json["url"]?.jsonPrimitive?.content.isNullOrEmpty()) {
            return fallbackFetchSongInfo(hash)
        }

        val url = json["url"]?.jsonPrimitive?.content ?: ""
        val bitRate = json["bitRate"]?.jsonPrimitive?.long ?: 0
        val extName = json["extName"]?.jsonPrimitive?.content ?: "mp3"
        val albumImg = json["album_img"]?.jsonPrimitive?.content ?: ""
        val songName = json["songName"]?.jsonPrimitive?.content ?: ""
        val authorName = json["author_name"]?.jsonPrimitive?.content ?: ""
        val timeLength = json["timeLength"]?.jsonPrimitive?.long ?: 0
        val fileSize = json["fileSize"]?.jsonPrimitive?.long ?: 0

        val cover = albumImg.replace("{size}", "240")

        return Song(
            id = hash,
            name = songName,
            artist = authorName,
            duration = timeLength,
            size = fileSize,
            bitrate = bitRate / 1000,
            ext = extName,
            source = "kugou",
            cover = cover,
            url = url,
            link = "https://www.kugou.com/song/#hash=$hash",
            extra = mapOf("hash" to hash)
        )
    }

    private suspend fun fallbackFetchSongInfo(hash: String): Song {
        val response = client.get("https://wwwapi.kugou.com/yy/index.php") {
            mobileHeaders("https://www.kugou.com/", cookie)
            url {
                parameters.append("r", "play/getdata")
                parameters.append("hash", hash)
                parameters.append("platid", "4")
            }
        }

        val json = response.body<JsonObject>()
        val data = json["data"]?.jsonObject ?: throw Exception("Download URL not found in fallback API")

        val playURL = data["play_url"]?.jsonPrimitive?.content
            ?: throw Exception("Download URL not found in fallback API")

        return Song(
            id = hash,
            name = data["song_name"]?.jsonPrimitive?.content ?: "",
            artist = data["author_name"]?.jsonPrimitive?.content ?: "",
            duration = (data["timelength"]?.jsonPrimitive?.long ?: 0) / 1000,
            size = data["filesize"]?.jsonPrimitive?.long ?: 0,
            bitrate = data["bitrate"]?.jsonPrimitive?.long ?: 0,
            source = "kugou",
            cover = data["img"]?.jsonPrimitive?.content ?: "",
            url = playURL,
            link = "https://www.kugou.com/song/#hash=$hash",
            extra = mapOf("hash" to hash)
        )
    }

    override suspend fun getLyrics(song: Song): String {
        if (song.source != "kugou") throw IllegalArgumentException("Source mismatch")
        val hash = song.extra["hash"] ?: song.id

        // Step 1: 搜索歌词
        val searchResponse = client.get("http://krcs.kugou.com/search") {
            mobileHeaders(MOBILE_REFERER, cookie)
            url {
                parameters.append("ver", "1")
                parameters.append("client", "mobi")
                parameters.append("hash", hash)
            }
        }

        val searchJson = searchResponse.body<JsonObject>()
        val candidates = searchJson["candidates"]?.jsonArray
            ?: throw Exception("Lyrics not found")

        if (candidates.isEmpty()) throw Exception("Lyrics not found")

        val candidate = candidates.first().jsonObject
        val id = candidate["id"]
        val accessKey = candidate["accesskey"]?.jsonPrimitive?.content ?: ""

        val idStr = when (id) {
            is JsonPrimitive -> id.content
            else -> id?.toString() ?: ""
        }

        // Step 2: 下载歌词
        val downloadResponse = client.get("http://lyrics.kugou.com/download") {
            mobileHeaders(MOBILE_REFERER, cookie)
            url {
                parameters.append("ver", "1")
                parameters.append("client", "pc")
                parameters.append("id", idStr)
                parameters.append("accesskey", accessKey)
                parameters.append("fmt", "lrc")
                parameters.append("charset", "utf8")
            }
        }

        val downloadJson = downloadResponse.body<JsonObject>()
        val content = downloadJson["content"]?.jsonPrimitive?.content
            ?: throw Exception("Lyrics content is empty")

        return String(Base64.getDecoder().decode(content))
    }

    override suspend fun getRecommendedPlaylists(): List<Playlist> {
        val response = client.get("http://m.kugou.com/plist/index&json=true") {
            mobileHeaders(MOBILE_REFERER, cookie)
        }

        val body = response.body<String>()
        if (body.isEmpty() || body[0] != '{') {
            throw Exception("Kugou API returned invalid JSON")
        }

        val json = Json.parseToJsonElement(body).jsonObject
        val playlists = mutableListOf<Playlist>()

        json["plist"]?.jsonObject?.get("list")?.jsonObject?.get("info")?.jsonArray?.forEach { itemEl ->
            val item = itemEl.jsonObject
            val specialID = item["specialid"]?.jsonPrimitive?.long ?: return@forEach
            val specialName = item["specialname"]?.jsonPrimitive?.content ?: ""
            val imgURL = item["imgurl"]?.jsonPrimitive?.content ?: ""
            val playCount = item["playcount"]?.jsonPrimitive?.long ?: 0
            val songCount = item["songcount"]?.jsonPrimitive?.long ?: 0
            val username = item["username"]?.jsonPrimitive?.content ?: ""
            val intro = item["intro"]?.jsonPrimitive?.content ?: ""

            val cover = imgURL.replace("{size}", "240")

            playlists.add(Playlist(
                id = specialID.toString(),
                name = specialName,
                cover = cover,
                trackCount = songCount,
                playCount = playCount,
                creator = username,
                description = intro,
                source = "kugou",
                link = "https://www.kugou.com/yy/special/single/$specialID.html"
            ))
        }

        return playlists
    }

    private fun isValidHash(hash: String?): Boolean {
        return !hash.isNullOrEmpty() && hash != "00000000000000000000000000000000"
    }
}
