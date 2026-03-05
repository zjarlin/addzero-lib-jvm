package site.addzero.network.call.musiclib.api.bilibili

import site.addzero.network.call.musiclib.model.Playlist
import site.addzero.network.call.musiclib.model.PlaylistDetail
import site.addzero.network.call.musiclib.model.Song
import site.addzero.network.call.musiclib.provider.CookieMusicProvider
import site.addzero.network.call.musiclib.utils.HttpClientManager
import site.addzero.network.call.musiclib.utils.defaultHeaders
import io.ktor.client.HttpClient
import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.serialization.json.*

/**
 * Bilibili API 实现
 */
class BilibiliAPI(
    override var cookie: String = "",
    private val client: HttpClient = HttpClientManager.client,
) : CookieMusicProvider {

    private var isVipCache: Boolean? = null

    override val name: String = "哔哩哔哩"
    override val source: String = "bilibili"

    companion object {
        const val USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36"
        const val REFERER = "https://www.bilibili.com/"
    }

    override suspend fun isVipAccount(): Boolean {
        isVipCache?.let { return it }
        if (cookie.isEmpty()) {
            isVipCache = false
            return false
        }

        val response = client.get("https://api.bilibili.com/x/web-interface/nav") {
            defaultHeaders(REFERER, cookie)
        }

        val json = response.body<JsonObject>()
        val code = json["code"]?.jsonPrimitive?.long ?: -1
        val data = json["data"]?.jsonObject
        val isLogin = data?.get("isLogin")?.jsonPrimitive?.boolean ?: false
        val vipStatus = data?.get("vipStatus")?.jsonPrimitive?.long ?: 0
        val vipType = data?.get("vipType")?.jsonPrimitive?.long ?: 0

        isVipCache = (code == 0.toLong() && isLogin && vipStatus == 1.toLong() && vipType > 0)
        return isVipCache!!
    }

    override suspend fun search(keyword: String): List<Song> {
        val response = client.get("https://api.bilibili.com/x/web-interface/search/type") {
            defaultHeaders(REFERER, cookie)
            url {
                parameters.append("search_type", "video")
                parameters.append("keyword", keyword)
                parameters.append("page", "1")
                parameters.append("page_size", "20")
            }
        }

        val json = response.body<JsonObject>()
        val songs = mutableListOf<Song>()

        json["data"]?.jsonObject?.get("result")?.jsonArray?.forEach { itemEl ->
            val item = itemEl.jsonObject
            val bvid = item["bvid"]?.jsonPrimitive?.content ?: return@forEach
            val title = cleanTitle(item["title"]?.jsonPrimitive?.content ?: "")
            val author = item["author"]?.jsonPrimitive?.content ?: ""
            val pic = item["pic"]?.jsonPrimitive?.content ?: ""

            val viewResp = fetchView(bvid) ?: return@forEach
            val pages = viewResp["data"]?.jsonObject?.get("pages")?.jsonArray ?: return@forEach
            if (pages.isEmpty()) return@forEach

            val firstPage = pages[0].jsonObject
            val cid = firstPage["cid"]?.jsonPrimitive?.long ?: return@forEach
            val part = firstPage["part"]?.jsonPrimitive?.content ?: ""
            val duration = firstPage["duration"]?.jsonPrimitive?.long ?: 0

            val displayTitle = if (part.isEmpty()) title else "$title - $part"
            val cover = normalizeCover(pic)

            songs.add(Song(
                id = "$bvid|$cid",
                name = displayTitle,
                artist = author,
                album = bvid,
                duration = duration,
                source = "bilibili",
                cover = cover,
                link = "https://www.bilibili.com/video/$bvid?p=1",
                extra = mapOf("bvid" to bvid, "cid" to cid.toString())
            ))
        }

        return songs
    }

    override suspend fun searchPlaylist(keyword: String): List<Playlist> {
        // Bilibili 搜索视频作为歌单
        val response = client.get("https://api.bilibili.com/x/web-interface/search/type") {
            defaultHeaders(REFERER, cookie)
            url {
                parameters.append("search_type", "video")
                parameters.append("keyword", keyword)
                parameters.append("page", "1")
                parameters.append("page_size", "20")
            }
        }

        val json = response.body<JsonObject>()
        val playlists = mutableListOf<Playlist>()
        val seen = mutableSetOf<String>()

        json["data"]?.jsonObject?.get("result")?.jsonArray?.forEach { itemEl ->
            val item = itemEl.jsonObject
            val bvid = item["bvid"]?.jsonPrimitive?.content ?: return@forEach

            val viewResp = fetchView(bvid) ?: return@forEach
            val data = viewResp["data"]?.jsonObject ?: return@forEach
            val pages = data["pages"]?.jsonArray ?: return@forEach

            if (pages.size <= 1) return@forEach
            if (seen.contains(bvid)) return@forEach
            seen.add(bvid)

            val title = cleanTitle(item["title"]?.jsonPrimitive?.content ?: "")
            val pic = normalizeCover(item["pic"]?.jsonPrimitive?.content ?: "")
            val owner = data["owner"]?.jsonObject
            val ownerName = owner?.get("name")?.jsonPrimitive?.content ?: ""

            playlists.add(Playlist(
                id = "bvid:$bvid",
                name = title,
                cover = pic,
                trackCount = pages.size.toLong(),
                creator = ownerName,
                source = "bilibili",
                link = "https://www.bilibili.com/video/$bvid",
                extra = mapOf("bvid" to bvid, "type" to "multipart")
            ))
        }

        return playlists
    }

    override suspend fun getPlaylistSongs(id: String): List<Song> {
        if (id.startsWith("season:")) {
            val parts = id.split(":")
            if (parts.size < 3) throw IllegalArgumentException("Invalid season id")
            val seasonID = parts[1].toLongOrNull() ?: 0
            val mid = parts[2].toLongOrNull() ?: 0
            return fetchSeasonSongs(mid, seasonID)
        }

        val bvid = id.removePrefix("bvid:")
        if (bvid.isEmpty()) throw IllegalArgumentException("Invalid playlist id")

        val viewResp = fetchView(bvid) ?: throw Exception("Failed to fetch view")
        val data = viewResp["data"]?.jsonObject ?: throw Exception("No data")
        val rootTitle = data["title"]?.jsonPrimitive?.content ?: ""
        val pic = data["pic"]?.jsonPrimitive?.content ?: ""
        val owner = data["owner"]?.jsonObject
        val ownerName = owner?.get("name")?.jsonPrimitive?.content ?: ""

        var pages = data["pages"]?.jsonArray
        if (pages == null || pages.isEmpty()) {
            pages = fetchPageList(bvid)
        }

        return pages?.mapIndexed { index, pageEl ->
            val page = pageEl.jsonObject
            val cid = page["cid"]?.jsonPrimitive?.long ?: 0
            val part = page["part"]?.jsonPrimitive?.content ?: ""
            val duration = page["duration"]?.jsonPrimitive?.long ?: 0

            val displayTitle = if (pages.size == 1 && part.isEmpty()) rootTitle
            else if (part != rootTitle) "$rootTitle - $part"
            else part

            Song(
                id = "$bvid|$cid",
                name = displayTitle,
                artist = ownerName,
                album = bvid,
                duration = duration,
                source = "bilibili",
                cover = normalizeCover(pic),
                link = "https://www.bilibili.com/video/$bvid?p=${index + 1}",
                extra = mapOf("bvid" to bvid, "cid" to cid.toString())
            )
        } ?: emptyList()
    }

    override suspend fun parsePlaylist(link: String): PlaylistDetail {
        val bvidRegex = "(BV\\w+)".toRegex()
        val match = bvidRegex.find(link) ?: throw IllegalArgumentException("Invalid bilibili link")
        val bvid = match.groupValues[1]

        val viewResp = fetchView(bvid) ?: throw Exception("Failed to fetch view")
        val data = viewResp["data"]?.jsonObject ?: throw Exception("No data")

        val rootTitle = data["title"]?.jsonPrimitive?.content ?: ""
        val pic = data["pic"]?.jsonPrimitive?.content ?: ""
        val owner = data["owner"]?.jsonObject
        val ownerName = owner?.get("name")?.jsonPrimitive?.content ?: ""

        var pages = data["pages"]?.jsonArray
        if (pages == null || pages.isEmpty()) {
            pages = fetchPageList(bvid)
        }

        val playlist = Playlist(
            id = "bvid:$bvid",
            name = rootTitle,
            cover = normalizeCover(pic),
            trackCount = (pages?.size ?: 0).toLong(),
            creator = ownerName,
            source = "bilibili",
            link = "https://www.bilibili.com/video/$bvid",
            extra = mapOf("bvid" to bvid, "type" to "multipart")
        )

        val songs = pages?.mapIndexed { index, pageEl ->
            val page = pageEl.jsonObject
            val cid = page["cid"]?.jsonPrimitive?.long ?: 0
            val part = page["part"]?.jsonPrimitive?.content ?: ""
            val duration = page["duration"]?.jsonPrimitive?.long ?: 0

            val displayTitle = if (pages.size == 1 && part.isEmpty()) rootTitle
            else if (part != rootTitle) "$rootTitle - $part"
            else part

            Song(
                id = "$bvid|$cid",
                name = displayTitle,
                artist = ownerName,
                album = bvid,
                duration = duration,
                source = "bilibili",
                cover = normalizeCover(pic),
                link = "https://www.bilibili.com/video/$bvid?p=${index + 1}",
                extra = mapOf("bvid" to bvid, "cid" to cid.toString())
            )
        } ?: emptyList()

        return PlaylistDetail(playlist, songs)
    }

    override suspend fun parse(link: String): Song {
        val bvidRegex = "(BV\\w+)".toRegex()
        val match = bvidRegex.find(link) ?: throw IllegalArgumentException("Invalid bilibili link")
        val bvid = match.groupValues[1]

        val pageRegex = "[?&]p=(\\d+)".toRegex()
        val pageMatch = pageRegex.find(link)
        val page = pageMatch?.groupValues?.get(1)?.toIntOrNull() ?: 1

        val viewResp = fetchView(bvid) ?: throw Exception("Failed to fetch view")
        val data = viewResp["data"]?.jsonObject ?: throw Exception("No data")
        val pages = data["pages"]?.jsonArray ?: throw Exception("No pages")

        if (pages.size > 1) throw Exception("Playlist link detected")

        val targetPage = pages.getOrNull(page - 1)?.jsonObject
            ?: pages.firstOrNull()?.jsonObject
            ?: throw Exception("Page not found")

        val cid = targetPage["cid"]?.jsonPrimitive?.long ?: 0
        val part = targetPage["part"]?.jsonPrimitive?.content ?: ""
        val duration = targetPage["duration"]?.jsonPrimitive?.long ?: 0
        val rootTitle = data["title"]?.jsonPrimitive?.content ?: ""
        val pic = data["pic"]?.jsonPrimitive?.content ?: ""
        val owner = data["owner"]?.jsonObject
        val ownerName = owner?.get("name")?.jsonPrimitive?.content ?: ""

        val displayTitle = if (part.isEmpty() || part == rootTitle) rootTitle else "$rootTitle - $part"
        val audioURL = fetchAudioURL(bvid, cid.toString())

        return Song(
            id = "$bvid|$cid",
            name = displayTitle,
            artist = ownerName,
            album = bvid,
            duration = duration,
            source = "bilibili",
            cover = normalizeCover(pic),
            url = audioURL,
            link = "https://www.bilibili.com/video/$bvid?p=$page",
            extra = mapOf("bvid" to bvid, "cid" to cid.toString())
        )
    }

    override suspend fun getDownloadURL(song: Song): String {
        if (song.source != "bilibili") throw IllegalArgumentException("Source mismatch")
        val bvid = song.extra["bvid"] ?: song.id.split("|").getOrNull(0) ?: throw Exception("BVID not found")
        val cid = song.extra["cid"] ?: song.id.split("|").getOrNull(1) ?: throw Exception("CID not found")
        return fetchAudioURL(bvid, cid)
    }

    override suspend fun getLyrics(song: Song): String {
        return ""  // Bilibili 没有歌词
    }

    private suspend fun fetchView(bvid: String): JsonObject? {
        return try {
            val response = client.get("https://api.bilibili.com/x/web-interface/view") {
                defaultHeaders(REFERER, cookie)
                parameter("bvid", bvid)
            }
            response.body<JsonObject>()
        } catch (e: Exception) {
            null
        }
    }

    private suspend fun fetchPageList(bvid: String): JsonArray? {
        return try {
            val response = client.get("https://api.bilibili.com/x/player/pagelist") {
                defaultHeaders(REFERER, cookie)
                parameter("bvid", bvid)
            }
            response.body<JsonObject>()["data"]?.jsonArray
        } catch (e: Exception) {
            null
        }
    }

    private suspend fun fetchAudioURL(bvid: String, cid: String): String {
        val isVip = isVipAccount()
        val fnval = if (isVip) 4048 else 80

        val response = client.get("https://api.bilibili.com/x/player/playurl") {
            defaultHeaders(REFERER, cookie)
            parameter("fnval", fnval)
            parameter("qn", 127)
            parameter("bvid", bvid)
            parameter("cid", cid)
        }

        val json = response.body<JsonObject>()
        val data = json["data"]?.jsonObject ?: throw Exception("No audio data")
        val dash = data["dash"]?.jsonObject

        // 优先获取 FLAC
        val flac = dash?.get("flac")?.jsonObject?.get("audio")?.jsonObject
        if (flac?.get("id")?.jsonPrimitive?.long == 30251.toLong()) {
            flac["baseUrl"]?.jsonPrimitive?.content?.let { return it }
        }

        // 其次获取 Dolby
        val dolby = dash?.get("dolby")?.jsonObject?.get("audio")?.jsonArray
        dolby?.forEach {
            val audio = it.jsonObject
            if (audio["id"]?.jsonPrimitive?.long == 30250.toLong()) {
                audio["baseUrl"]?.jsonPrimitive?.content?.let { url -> return url }
            }
        }

        // 获取最佳音质的 DASH 音频
        val audios = dash?.get("audio")?.jsonArray
        val bestAudio = audios?.maxByOrNull {
            it.jsonObject["id"]?.jsonPrimitive?.long ?: 0
        }?.jsonObject
        bestAudio?.get("baseUrl")?.jsonPrimitive?.content?.let { return it }

        // 回退到 DURL
        val durl = data["durl"]?.jsonArray
        durl?.firstOrNull()?.jsonObject?.get("url")?.jsonPrimitive?.content?.let { return it }

        throw Exception("No audio found")
    }

    private suspend fun fetchSeasonSongs(mid: Long, seasonID: Long): List<Song> {
        val songs = mutableListOf<Song>()
        var pageNum = 1
        val pageSize = 30
        var processedArchives = 0

        while (true) {
            val response = client.get("https://api.bilibili.com/x/space/ugc/season") {
                defaultHeaders(REFERER, cookie)
                parameter("mid", mid)
                parameter("season_id", seasonID)
                parameter("page_num", pageNum)
                parameter("page_size", pageSize)
            }

            val json = response.body<JsonObject>()
            val code = json["code"]?.jsonPrimitive?.long ?: -1
            if (code != 0.toLong()) break

            val data = json["data"]?.jsonObject ?: break
            val archives = data["archives"]?.jsonArray ?: break
            if (archives.isEmpty()) break

            archives.forEach { arcEl ->
                val arc = arcEl.jsonObject
                val arcBvid = arc["bvid"]?.jsonPrimitive?.content ?: return@forEach
                val arcCid = arc["cid"]?.jsonPrimitive?.long ?: 0
                val arcTitle = arc["title"]?.jsonPrimitive?.content ?: ""
                val arcCover = arc["cover"]?.jsonPrimitive?.content ?: ""
                val arcDuration = arc["duration"]?.jsonPrimitive?.long ?: 0

                if (arcCid != 0L) {
                    songs.add(Song(
                        id = "$arcBvid|$arcCid",
                        name = arcTitle,
                        artist = "",
                        album = "",
                        duration = arcDuration,
                        source = "bilibili",
                        cover = normalizeCover(arcCover),
                        link = "https://www.bilibili.com/video/$arcBvid",
                        extra = mapOf("bvid" to arcBvid, "cid" to arcCid.toString())
                    ))
                }
            }

            processedArchives += archives.size
            val total = data["page"]?.jsonObject?.get("total")?.jsonPrimitive?.long ?: 0
            if (total == 0.toLong() || processedArchives >= total) break
            pageNum++
        }

        return songs
    }

    private fun cleanTitle(title: String): String {
        return title
            .replace("<em class=\"keyword\">", "")
            .replace("</em>", "")
    }

    private fun normalizeCover(cover: String): String {
        return if (cover.startsWith("//")) "https:$cover" else cover
    }
}
