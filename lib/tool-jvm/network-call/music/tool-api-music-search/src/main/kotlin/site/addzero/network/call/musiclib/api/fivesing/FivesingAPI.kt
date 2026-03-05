package site.addzero.network.call.musiclib.api.fivesing

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
import org.unbescape.html.HtmlEscape


/**
 * 5sing API 实现
 */
class FivesingAPI(
    override var cookie: String = "",
    private val client: HttpClient = HttpClientManager.client,
) : CookieMusicProvider {

    override val name: String = "5sing"
    override val source: String = "fivesing"

    companion object {
        const val USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36"
    }

    private fun HttpRequestBuilder.fivesingHeaders() {
        header(HttpHeaders.UserAgent, USER_AGENT)
        header(HttpHeaders.Cookie, cookie)
    }

    override suspend fun search(keyword: String): List<Song> {
        val response = client.get("http://search.5sing.kugou.com/home/json") {
            fivesingHeaders()
            parameter("keyword", keyword)
            parameter("sort", "1")
            parameter("page", "1")
            parameter("filter", "0")
            parameter("type", "0")
        }

        val json = response.body<JsonObject>()
        val songs = mutableListOf<Song>()

        json["list"]?.jsonArray?.forEach { itemEl ->
            val item = itemEl.jsonObject
            val songID = item["songId"]?.jsonPrimitive?.long ?: return@forEach
            val songName = HtmlEscape.unescapeHtml(item["songName"]?.jsonPrimitive?.content ?: "")
            val singer = HtmlEscape.unescapeHtml(item["singer"]?.jsonPrimitive?.content ?: "")
            val songSize = item["songSize"]?.jsonPrimitive?.long ?: 0
            val typeEname = item["typeEname"]?.jsonPrimitive?.content ?: "yc"

            val duration = if (songSize > 0) ((songSize * 8) / 320000) else 0

            songs.add(Song(
                id = "$songID|$typeEname",
                name = removeEmTags(songName),
                artist = removeEmTags(singer),
                duration = duration,
                size = songSize,
                source = "fivesing",
                link = "http://5sing.kugou.com/$typeEname/$songID.html",
                extra = mapOf("songid" to songID.toString(), "songtype" to typeEname)
            ))
        }

        return songs
    }

    override suspend fun searchPlaylist(keyword: String): List<Playlist> {
        val response = client.get("http://search.5sing.kugou.com/home/json") {
            fivesingHeaders()
            parameter("keyword", keyword)
            parameter("sort", "1")
            parameter("page", "1")
            parameter("filter", "0")
            parameter("type", "1")
        }

        val json = response.body<JsonObject>()
        val playlists = mutableListOf<Playlist>()

        json["list"]?.jsonArray?.forEach { itemEl ->
            val item = itemEl.jsonObject
            val songListId = item["songListId"]?.jsonPrimitive?.content ?: return@forEach
            val title = HtmlEscape.unescapeHtml(item["title"]?.jsonPrimitive?.content ?: "")
            val picture = item["pictureUrl"]?.jsonPrimitive?.content ?: ""
            val playCount = item["playCount"]?.jsonPrimitive?.long ?: 0
            val userName = item["userName"]?.jsonPrimitive?.content ?: ""
            val songCnt = item["songCnt"]?.jsonPrimitive?.long ?: 0
            val content = HtmlEscape.unescapeHtml(item["content"]?.jsonPrimitive?.content ?: "")
            val userId = item["userId"]?.jsonPrimitive?.content ?: ""

            val desc = if (content == "0") "" else content
            val creator = if (userName.isEmpty()) "ID: $userId" else userName
            val link = if (userId.isNotEmpty()) "http://5sing.kugou.com/$userId/dj/$songListId.html" else ""

            playlists.add(Playlist(
                id = songListId,
                name = removeEmTags(title),
                cover = picture,
                trackCount = songCnt,
                playCount = playCount,
                creator = creator,
                description = desc,
                source = "fivesing",
                link = link,
                extra = mapOf("user_id" to userId)
            ))
        }

        return playlists
    }

    override suspend fun getPlaylistSongs(id: String): List<Song> {
        val (_, songs) = fetchPlaylistDetail(id)
        return songs
    }

    override suspend fun parsePlaylist(link: String): PlaylistDetail {
        val regex = "5sing\\.kugou\\.com/(?:(\\d+)/)?dj/([a-zA-Z0-9]+)\\.html".toRegex()
        val match = regex.find(link) ?: throw IllegalArgumentException("Invalid 5sing playlist link")
        val playlistId = match.groupValues[2]
        return fetchPlaylistDetail(playlistId)
    }

    private suspend fun fetchPlaylistDetail(id: String): PlaylistDetail {
        val infoURL = "http://mobileapi.5sing.kugou.com/song/getsonglist?id=$id&songfields=ID,user"
        val infoResponse = client.get(infoURL) { fivesingHeaders() }
        val infoJson = infoResponse.body<JsonObject>()

        val rawData = infoJson["data"]
        val data = when (rawData) {
            is JsonObject -> rawData
            else -> throw Exception("Playlist info not found")
        }

        val user = data["user"]?.jsonObject
        val userId = user?.get("ID")?.jsonPrimitive?.long ?: 0
        val userName = user?.get("NN")?.jsonPrimitive?.content ?: ""

        if (userId == 0L) throw Exception("Playlist user not found")

        val playlist = Playlist(
            id = id,
            name = data["T"]?.jsonPrimitive?.content ?: "",
            cover = data["P"]?.jsonPrimitive?.content ?: "",
            playCount = data["H"]?.jsonPrimitive?.long ?: 0,
            trackCount = data["E"]?.jsonPrimitive?.long ?: 0,
            creator = userName,
            description = data["C"]?.jsonPrimitive?.content ?: "",
            source = "fivesing",
            link = "http://5sing.kugou.com/$userId/dj/$id.html",
            extra = mapOf("user_id" to userId.toString())
        )

        val pageURL = playlist.link
        val htmlResponse = client.get(pageURL) { fivesingHeaders() }
        val htmlBody = htmlResponse.body<String>()
        val songs = parseSongsFromHTML(htmlBody)

        return PlaylistDetail(playlist, songs)
    }

    private fun parseSongsFromHTML(htmlContent: String): List<Song> {
        val songs = mutableListOf<Song>()
        val seen = mutableSetOf<String>()

        // 提取所有 <li class="p_rel">...</li> 块
        val blockRegex = "<li class=\"p_rel\">([\\s\\S]*?)</li>".toRegex()
        val blocks = blockRegex.findAll(htmlContent)

        val songRegex = "href=\"http://5sing\\.kugou\\.com/(yc|fc|bz)/(\\d+)\\.html\"[^>]*>([^<]+)</a>".toRegex()
        val artistRegex = "class=\"s_soner[^\"]*\".*?>([^<]+)</a>".toRegex()

        blocks.forEach { blockMatch ->
            val blockHTML = blockMatch.groupValues[1]

            val songMatch = songRegex.find(blockHTML)
            if (songMatch == null || songMatch.groupValues.size < 4) return@forEach

            val kind = songMatch.groupValues[1]
            val songID = songMatch.groupValues[2]
            val rawName = songMatch.groupValues[3]

            var artist = "Unknown"
            val artistMatch = artistRegex.find(blockHTML)
            if (artistMatch != null && artistMatch.groupValues.size >= 2) {
                artist = artistMatch.groupValues[1]
            }

            val uniqueKey = "$kind|$songID"
            if (seen.contains(uniqueKey)) return@forEach
            seen.add(uniqueKey)

            val name = HtmlEscape.unescapeHtml(rawName).trim()

            songs.add(Song(
                id = "$songID|$kind",
                name = name,
                artist = HtmlEscape.unescapeHtml(artist).trim(),
                source = "fivesing",
                link = "http://5sing.kugou.com/$kind/$songID.html",
                extra = mapOf("songid" to songID, "songtype" to kind)
            ))
        }

        return songs
    }

    override suspend fun parse(link: String): Song {
        val regex = "5sing\\.kugou\\.com/(\\w+)/(\\d+)\\.html".toRegex()
        val match = regex.find(link) ?: throw IllegalArgumentException("Invalid 5sing link")
        val songType = match.groupValues[1]
        val songID = match.groupValues[2]
        return fetchSongInfo(songID, songType)
    }

    override suspend fun getDownloadURL(song: Song): String {
        if (song.source != "fivesing") throw IllegalArgumentException("Source mismatch")
        val songID = song.extra["songid"] ?: song.id.split("|").getOrNull(0) ?: throw Exception("Song ID not found")
        val songType = song.extra["songtype"] ?: song.id.split("|").getOrNull(1) ?: throw Exception("Song type not found")
        return fetchAudioLink(songID, songType)
    }

    private suspend fun fetchSongInfo(songID: String, songType: String): Song {
        val audioURL = fetchAudioLink(songID, songType)

        val params = mapOf("songid" to songID, "songtype" to songType)
        val metaURL = "http://mobileapi.5sing.kugou.com/song/newget?${params.entries.joinToString("&") { "${it.key}=${it.value}" }}"

        val metaResponse = client.get(metaURL) { fivesingHeaders() }
        val metaJson = try {
            metaResponse.body<JsonObject>()
        } catch (e: Exception) {
            null
        }

        val data = metaJson?.get("data")?.jsonObject
        val sn = data?.get("SN")?.jsonPrimitive?.content ?: ""
        val nn = data?.get("user")?.jsonObject?.get("NN")?.jsonPrimitive?.content ?: ""
        val i = data?.get("user")?.jsonObject?.get("I")?.jsonPrimitive?.content ?: ""

        val name = if (sn.isNotEmpty()) sn else "5sing_${songType}_$songID"

        return Song(
            id = "$songID|$songType",
            name = name,
            artist = nn,
            cover = i,
            url = audioURL,
            source = "fivesing",
            link = "http://5sing.kugou.com/$songType/$songID.html",
            extra = mapOf("songid" to songID, "songtype" to songType)
        )
    }

    private suspend fun fetchAudioLink(songID: String, songType: String): String {
        val params = mapOf("songid" to songID, "songtype" to songType)
        val apiURL = "http://mobileapi.5sing.kugou.com/song/getSongUrl?${params.entries.joinToString("&") { "${it.key}=${it.value}" }}"

        val response = client.get(apiURL) { fivesingHeaders() }
        val json = response.body<JsonObject>()

        val code = json["code"]?.jsonPrimitive?.long ?: -1
        if (code != 1000.toLong()) throw Exception("API returned error code")

        val data = json["data"]?.jsonObject ?: throw Exception("No data")

        listOf("squrl", "squrl_backup", "hqurl", "hqurl_backup", "lqurl", "lqurl_backup").forEach { key ->
            data[key]?.jsonPrimitive?.content?.let { if (it.isNotEmpty()) return it }
        }

        throw Exception("No valid download URL found")
    }

    override suspend fun getLyrics(song: Song): String {
        if (song.source != "fivesing") throw IllegalArgumentException("Source mismatch")
        val songID = song.extra["songid"] ?: song.id.split("|").getOrNull(0) ?: throw Exception("Song ID not found")
        val songType = song.extra["songtype"] ?: song.id.split("|").getOrNull(1) ?: throw Exception("Song type not found")

        val params = mapOf("songid" to songID, "songtype" to songType)
        val apiURL = "http://mobileapi.5sing.kugou.com/song/newget?${params.entries.joinToString("&") { "${it.key}=${it.value}" }}"

        val response = client.get(apiURL) { fivesingHeaders() }
        val json = response.body<JsonObject>()

        return json["data"]?.jsonObject?.get("dynamicWords")?.jsonPrimitive?.content
            ?: throw Exception("Lyrics not found")
    }

    private fun removeEmTags(s: String): String {
        return s.replace("<em class=\"keyword\">", "").replace("</em>", "").trim()
    }
}
