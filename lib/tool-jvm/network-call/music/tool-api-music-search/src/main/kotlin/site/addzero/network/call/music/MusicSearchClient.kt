package site.addzero.network.call.music

import com.alibaba.fastjson2.JSON
import okhttp3.OkHttpClient
import okhttp3.Request
import site.addzero.network.call.music.model.*
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

/**
 * 音乐搜索客户端
 * 基于网易云音乐 API
 *
 * @param baseUrl API 基础 URL，默认使用公开的网易云音乐 API
 */
object MusicSearchUtil {
  private const val baseUrl: String = "https://music.163.com/api"

  private val client = OkHttpClient.Builder()
    .connectTimeout(10, TimeUnit.SECONDS)
    .readTimeout(30, TimeUnit.SECONDS)
    .build()

  /**
   * 搜索音乐
   *
   * @param request 搜索请求
   * @return 搜索结果
   */
  fun search(request: MusicSearchRequest): SearchResult? {
    val keywords = URLEncoder.encode(request.keywords, "UTF-8")
    val url = "$baseUrl/search/get/web?" +
      "s=${keywords}&" +
      "type=${request.type.value}&" +
      "limit=${request.limit}&" +
      "offset=${request.offset}"

    val httpRequest = Request.Builder()
      .url(url)
      .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
      .addHeader("Referer", "https://music.163.com/")
      .get()
      .build()

    client.newCall(httpRequest).execute().use { response ->
      val responseBody = response.body?.string()
        ?: throw RuntimeException("响应为空")

      if (!response.isSuccessful) {
        throw RuntimeException("请求失败: ${response.code}")
      }

      val result = JSON.parseObject(responseBody, MusicSearchResponse::class.java)
      if (result.code != 200) {
        throw RuntimeException("API 错误: ${result.code}")
      }

      return result.result
    }
  }

  /**
   * 搜索歌曲
   *
   * @param keywords 搜索关键词
   * @param limit 返回数量
   * @param offset 偏移量
   * @return 歌曲列表
   */
  fun searchSongs(keywords: String, limit: Int = 30, offset: Int = 0): List<Song> {
    val request = MusicSearchRequest(keywords, SearchType.SONG, limit, offset)
    return search(request)?.songs ?: emptyList()
  }

  /**
   * 搜索歌手
   *
   * @param keywords 搜索关键词
   * @param limit 返回数量
   * @param offset 偏移量
   * @return 歌手列表
   */
  fun searchArtists(keywords: String, limit: Int = 30, offset: Int = 0): List<Artist> {
    val request = MusicSearchRequest(keywords, SearchType.ARTIST, limit, offset)
    return search(request)?.artists ?: emptyList()
  }

  /**
   * 搜索专辑
   *
   * @param keywords 搜索关键词
   * @param limit 返回数量
   * @param offset 偏移量
   * @return 专辑列表
   */
  fun searchAlbums(keywords: String, limit: Int = 30, offset: Int = 0): List<Album> {
    val request = MusicSearchRequest(keywords, SearchType.ALBUM, limit, offset)
    return search(request)?.albums ?: emptyList()
  }

  /**
   * 搜索歌单
   *
   * @param keywords 搜索关键词
   * @param limit 返回数量
   * @param offset 偏移量
   * @return 歌单列表
   */
  fun searchPlaylists(keywords: String, limit: Int = 30, offset: Int = 0): List<Playlist> {
    val request = MusicSearchRequest(keywords, SearchType.PLAYLIST, limit, offset)
    return search(request)?.playlists ?: emptyList()
  }

  /**
   * 获取歌词
   *
   * @param songId 歌曲 ID
   * @return 歌词信息
   */
  fun getLyric(songId: Long): LyricResponse {
    val url = "$baseUrl/song/lyric?id=$songId&lv=1&tv=1"

    val httpRequest = Request.Builder()
      .url(url)
      .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
      .addHeader("Referer", "https://music.163.com/")
      .get()
      .build()

    client.newCall(httpRequest).execute().use { response ->
      val responseBody = response.body?.string()
        ?: throw RuntimeException("响应为空")

      if (!response.isSuccessful) {
        throw RuntimeException("请求失败: ${response.code}")
      }

      val result = JSON.parseObject(responseBody, LyricResponse::class.java)
      if (result.code != 200) {
        throw RuntimeException("API 错误: ${result.code}")
      }

      return result
    }
  }

  /**
   * 获取歌曲详情
   *
   * @param songIds 歌曲 ID 列表
   * @return 歌曲详情列表
   */
  fun getSongDetail(songIds: List<Long>): List<Song> {
    val ids = songIds.joinToString(",")
    val url = "$baseUrl/song/detail?ids=[$ids]"

    val httpRequest = Request.Builder()
      .url(url)
      .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
      .addHeader("Referer", "https://music.163.com/")
      .get()
      .build()

    client.newCall(httpRequest).execute().use { response ->
      val responseBody = response.body?.string()
        ?: throw RuntimeException("响应为空")

      if (!response.isSuccessful) {
        throw RuntimeException("请求失败: ${response.code}")
      }

      val result = JSON.parseObject(responseBody, SongDetailResponse::class.java)
      if (result.code != 200) {
        throw RuntimeException("API 错误: ${result.code}")
      }

      return result.songs ?: emptyList()
    }
  }

  /**
   * 根据歌名和歌手搜索歌曲
   *
   * @param songName 歌名
   * @param artistName 歌手名（可选）
   * @return 匹配的歌曲列表
   */
  fun searchBySongAndArtist(songName: String, artistName: String? = null): List<Song> {
    val keywords = if (artistName != null) {
      "$songName $artistName"
    } else {
      songName
    }

    val songs = searchSongs(keywords, limit = 10)

    // 如果指定了歌手，进行二次过滤
    return if (artistName != null) {
      songs.filter { song ->
        song.artists.any { artist ->
          artist.name.contains(artistName, ignoreCase = true)
        }
      }
    } else {
      songs
    }
  }

  /**
   * 根据歌词片段搜索歌曲
   *
   * @param lyricFragment 歌词片段
   * @return 匹配的歌曲列表
   */
  fun searchByLyric(lyricFragment: String): List<Song> {
    val request = MusicSearchRequest(lyricFragment, SearchType.LYRIC, limit = 20)
    return search(request)?.songs ?: emptyList()
  }

  /**
   * 根据歌名获取歌词
   *
   * @param songName 歌名
   * @param artistName 歌手名（可选，用于精确匹配）
   * @return 歌词响应，如果找不到返回 null
   */
  fun getLyricBySongName(songName: String, artistName: String? = null): LyricResponse? {
    val songs = searchBySongAndArtist(songName, artistName)
    if (songs.isEmpty()) {
      return null
    }
    return getLyric(songs.first().id)
  }

  /**
   * 根据歌词片段获取完整歌词
   *
   * @param lyricFragment 歌词片段
   * @param limit 返回数量限制，默认 5
   * @param filterEmpty 是否过滤空歌词，默认 true
   * @return 歌曲与歌词组合列表
   */
  fun getLyricsByFragment(lyricFragment: String, limit: Int = 5, filterEmpty: Boolean = true): List<SongWithLyric> {
    val songs = searchByLyric(lyricFragment).take(limit)
    return songs.mapNotNull { song ->
      try {
        val lyric = getLyric(song.id)
        // 如果开启过滤且歌词为空，则跳过
        if (filterEmpty && lyric.lrc?.lyric.isNullOrBlank()) {
          null
        } else {
          SongWithLyric(song, lyric)
        }
      } catch (e: Exception) {
        null
      }
    }
  }
}
