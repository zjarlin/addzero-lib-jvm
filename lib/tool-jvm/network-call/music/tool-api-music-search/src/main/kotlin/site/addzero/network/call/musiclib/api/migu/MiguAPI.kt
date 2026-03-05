package site.addzero.network.call.musiclib.api.migu

import site.addzero.network.call.musiclib.model.Playlist
import site.addzero.network.call.musiclib.model.PlaylistDetail
import site.addzero.network.call.musiclib.model.Song
import site.addzero.network.call.musiclib.provider.CookieMusicProvider
import site.addzero.network.call.musiclib.utils.HttpClientManager
import site.addzero.network.call.musiclib.utils.mobileHeaders
import io.ktor.client.HttpClient
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.json.*

/**
 * 咪咕音乐 API 实现
 */
class MiguAPI(
    override var cookie: String = "",
    private val client: HttpClient = HttpClientManager.client,
) : CookieMusicProvider {

    override val name: String = "咪咕音乐"
    override val source: String = "migu"

    companion object {
        const val USER_AGENT = "Mozilla/5.0 (iPhone; CPU iPhone OS 9_1 like Mac OS X)"
        const val REFERER = "http://music.migu.cn/"
        const val MAGIC_USER_ID = "15548614588710179085069"
    }

    override suspend fun search(keyword: String): List<Song> {
        val response = client.get("http://pd.musicapp.migu.cn/MIGUM2.0/v1.0/content/search_all.do") {
            mobileHeaders(REFERER, cookie)
            url {
                parameters.append("ua", "Android_migu")
                parameters.append("version", "5.0.1")
                parameters.append("text", keyword)
                parameters.append("pageNo", "1")
                parameters.append("pageSize", "10")
                parameters.append("searchSwitch", """{"song":1,"album":0,"singer":0,"tagSong":0,"mvSong":0,"songlist":0,"bestShow":1}""")
            }
        }

        val json = response.body<JsonObject>()
        val songs = mutableListOf<Song>()

        json["songResultData"]?.jsonObject?.get("result")?.jsonArray?.forEach { itemEl ->
            val item = itemEl.jsonObject
            val song = convertItemToSong(item) ?: return@forEach
            songs.add(song)
        }

        return songs
    }

    override suspend fun searchPlaylist(keyword: String): List<Playlist> {
        val response = client.get("http://pd.musicapp.migu.cn/MIGUM2.0/v1.0/content/search_all.do") {
            mobileHeaders(REFERER, cookie)
            url {
                parameters.append("ua", "Android_migu")
                parameters.append("version", "5.0.1")
                parameters.append("text", keyword)
                parameters.append("pageNo", "1")
                parameters.append("pageSize", "10")
                parameters.append("searchSwitch", """{"song":0,"album":0,"singer":0,"tagSong":0,"mvSong":0,"songlist":1,"bestShow":1}""")
            }
        }

        val json = response.body<JsonObject>()
        val playlists = mutableListOf<Playlist>()

        json["songListResultData"]?.jsonObject?.get("result")?.jsonArray?.forEach { itemEl ->
            val item = itemEl.jsonObject
            val id = item["id"]?.jsonPrimitive?.content ?: return@forEach
            val name = item["name"]?.jsonPrimitive?.content ?: ""
            val musicNum = item["musicNum"]?.jsonPrimitive?.content ?: "0"
            val userName = item["userName"]?.jsonPrimitive?.content ?: ""
            val imgItems = item["imgItems"]?.jsonArray
            val cover = imgItems?.firstOrNull()?.jsonObject?.get("img")?.jsonPrimitive?.content ?: ""

            playlists.add(Playlist(
                id = id,
                name = name,
                cover = cover,
                trackCount = (musicNum.toIntOrNull() ?: 0).toLong(),
                creator = userName,
                source = "migu"
            ))
        }

        return playlists
    }

    override suspend fun getPlaylistSongs(id: String): List<Song> {
        val response = client.get("http://c.musicapp.migu.cn/MIGUM2.0/v1.0/content/musicListContent.do") {
            mobileHeaders(REFERER, cookie)
            url {
                parameters.append("musicListId", id)
                parameters.append("pageNo", "1")
                parameters.append("pageSize", "100")
            }
        }

        val json = response.body<JsonObject>()
        val code = json["code"]?.jsonPrimitive?.content ?: ""
        if (code != "000000") {
            throw Exception("Migu API error: ${json["info"]?.jsonPrimitive?.content}")
        }

        val songs = mutableListOf<Song>()

        json["contentList"]?.jsonArray?.forEach { itemEl ->
            val item = itemEl.jsonObject
            val contentId = item["contentId"]?.jsonPrimitive?.content ?: ""
            val songId = item["songId"]?.jsonPrimitive?.content ?: ""
            val songName = item["songName"]?.jsonPrimitive?.content ?: ""
            val singerName = item["singerName"]?.jsonPrimitive?.content ?: ""
            val albumName = item["albumName"]?.jsonPrimitive?.content ?: ""
            val picM = item["picM"]?.jsonPrimitive?.content ?: ""
            val picL = item["picL"]?.jsonPrimitive?.content ?: ""
            val copyrightId = item["copyrightId"]?.jsonPrimitive?.content ?: ""

            val id = if (contentId.isNotEmpty()) contentId else songId
            val cover = if (picL.isNotEmpty()) picL else picM
            val finalCover = if (cover.isNotEmpty() && !cover.startsWith("http")) "http:$cover" else cover

            songs.add(Song(
                id = id,
                name = songName,
                artist = singerName,
                album = albumName,
                source = "migu",
                cover = finalCover,
                link = "https://music.migu.cn/v3/music/song/$copyrightId",
                extra = mapOf("content_id" to contentId, "copyright_id" to copyrightId)
            ))
        }

        return songs
    }

    override suspend fun parsePlaylist(link: String): PlaylistDetail {
        throw NotImplementedError("Migu playlist parsing not implemented")
    }

    override suspend fun parse(link: String): Song {
        val regex = "music\\.migu\\.cn/v3/music/song/(\\d+)".toRegex()
        val match = regex.find(link) ?: throw IllegalArgumentException("Invalid migu link")
        val contentID = match.groupValues[1]

        val song = fetchSongDetail(contentID)
        val downloadURL = getDownloadURL(song)
        return song.copy(url = downloadURL)
    }

    private suspend fun fetchSongDetail(contentID: String): Song {
        val response = client.get("http://c.musicapp.migu.cn/MIGUM2.0/v1.0/content/queryById.do") {
            mobileHeaders(REFERER, cookie)
            url {
                parameters.append("resourceType", "2")
                parameters.append("contentId", contentID)
            }
        }

        val json = response.body<JsonObject>()
        val resource = json["resource"]?.jsonArray?.firstOrNull()?.jsonObject
            ?: json["data"]?.jsonObject?.get("item")?.jsonObject
            ?: throw Exception("Song detail not found")

        return convertItemToSong(resource) ?: throw Exception("No valid format found")
    }

    private fun convertItemToSong(item: JsonObject): Song? {
        val contentId = item["contentId"]?.jsonPrimitive?.content ?: ""
        val id = item["id"]?.jsonPrimitive?.content ?: contentId
        val name = item["name"]?.jsonPrimitive?.content ?: ""

        val singers = item["singers"]?.jsonArray?.map {
            it.jsonObject["name"]?.jsonPrimitive?.content ?: ""
        } ?: emptyList()

        val albums = item["albums"]?.jsonArray
        val albumName = albums?.firstOrNull()?.jsonObject?.get("name")?.jsonPrimitive?.content ?: ""

        val imgItems = item["imgItems"]?.jsonArray
        val cover = imgItems?.firstOrNull()?.jsonObject?.get("img")?.jsonPrimitive?.content ?: ""

        val rateFormats = item["rateFormats"]?.jsonArray
        if (rateFormats.isNullOrEmpty()) return null

        // 选择最佳音质
        var bestFormat: JsonObject? = null
        var bestSize = 0L

        rateFormats.forEach { fmtEl ->
            val fmt = fmtEl.jsonObject
            val sizeStr = fmt["androidSize"]?.jsonPrimitive?.content
                ?: fmt["size"]?.jsonPrimitive?.content ?: "0"
            val size = sizeStr.toLongOrNull() ?: 0
            if (size > bestSize) {
                bestSize = size
                bestFormat = fmt
            }
        }

        bestFormat ?: return null

        val resourceType = bestFormat!!["resourceType"]?.jsonPrimitive?.content ?: ""
        val formatType = bestFormat!!["formatType"]?.jsonPrimitive?.content ?: ""
        val ext = bestFormat!!["androidFileType"]?.jsonPrimitive?.content
            ?: bestFormat!!["fileType"]?.jsonPrimitive?.content ?: "mp3"

        return Song(
            id = "$contentId|$resourceType|$formatType",
            name = name,
            artist = singers.joinToString("、"),
            album = albumName,
            size = bestSize,
            source = "migu",
            ext = ext,
            cover = cover,
            link = "https://music.migu.cn/v3/music/song/$contentId",
            extra = mapOf(
                "content_id" to contentId,
                "resource_type" to resourceType,
                "format_type" to formatType
            )
        )
    }

    override suspend fun getDownloadURL(song: Song): String {
        if (song.source != "migu") throw IllegalArgumentException("Source mismatch")

        val parts = song.id.split("|")
        val contentID = song.extra["content_id"] ?: parts.getOrNull(0) ?: throw Exception("Content ID not found")
        val resourceType = song.extra["resource_type"] ?: parts.getOrNull(1) ?: "2"
        val formatType = song.extra["format_type"] ?: parts.getOrNull(2) ?: "PQ"

        val noRedirectClient = client.config {
            followRedirects = false
        }

        val response = noRedirectClient.get("http://app.pd.nf.migu.cn/MIGUM2.0/v1.0/content/sub/listenSong.do") {
            mobileHeaders(REFERER, cookie)
            url {
                parameters.append("toneFlag", formatType)
                parameters.append("netType", "00")
                parameters.append("userId", MAGIC_USER_ID)
                parameters.append("ua", "Android_migu")
                parameters.append("version", "5.1")
                parameters.append("copyrightId", "0")
                parameters.append("contentId", contentID)
                parameters.append("resourceType", resourceType)
                parameters.append("channel", "0")
            }
        }

        if (response.status == HttpStatusCode.Found) {
            val location = response.headers[HttpHeaders.Location]
            if (!location.isNullOrEmpty()) {
                return location
            }
        }

        throw Exception("Failed to get download URL")
    }

    override suspend fun getLyrics(song: Song): String {
        if (song.source != "migu") throw IllegalArgumentException("Source mismatch")

        val contentID = song.extra["content_id"] ?: song.id.split("|").firstOrNull()
        ?: throw Exception("Content ID not found")

        val response = client.get("http://c.musicapp.migu.cn/MIGUM2.0/v1.0/content/resourceinfo.do") {
            mobileHeaders(REFERER, cookie)
            url {
                parameters.append("resourceId", contentID)
                parameters.append("resourceType", "2")
            }
        }

        val json = response.body<JsonObject>()
        val resource = json["resource"]?.jsonArray?.firstOrNull()?.jsonObject
            ?: throw Exception("Resource not found")

        val lyricUrl = resource["lrcUrl"]?.jsonPrimitive?.content
            ?: resource["lyricUrl"]?.jsonPrimitive?.content
            ?: throw Exception("Lyric URL not found")

        val lrcResponse = client.get(lyricUrl.replace("http://", "https://")) {
            mobileHeaders("https://y.migu.cn/", cookie)
        }

        return lrcResponse.body<String>()
    }
}
