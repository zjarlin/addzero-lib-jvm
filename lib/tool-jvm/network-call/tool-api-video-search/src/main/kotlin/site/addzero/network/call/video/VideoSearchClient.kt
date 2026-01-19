package site.addzero.network.call.video

import okhttp3.OkHttpClient
import okhttp3.Request
import site.addzero.network.call.video.model.*
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

class VideoSearchClient(
  private val baseUrl: String = "https://api.example.com"
) {

  private val client = OkHttpClient.Builder()
    .connectTimeout(10, TimeUnit.SECONDS)
    .readTimeout(30, TimeUnit.SECONDS)
    .build()

  companion object {
    private val VIDEO_SEARCH_SOURCES = listOf(
      VideoSearchSource(
        name = "豆瓣",
        baseUrl = "https://movie.douban.com",
        searchPath = "/j/search_subjects",
        type = "MOVIE",
        enabled = true
      ),
      VideoSearchSource(
        name = "IMDb",
        baseUrl = "https://v2.sg.media-imdb.com",
        searchPath = "/suggests",
        type = "MOVIE",
        enabled = true
      ),
      VideoSearchSource(
        name = "TheMovieDB",
        baseUrl = "https://api.themoviedb.org",
        searchPath = "/3/search/multi",
        type = "MOVIE",
        enabled = false
      ),
      VideoSearchSource(
        name = "OMDb",
        baseUrl = "https://www.omdbapi.com",
        searchPath = "/",
        type = "MOVIE",
        enabled = false
      )
    )

    @Volatile
    private var instance: VideoSearchClient? = null

    fun getInstance(): VideoSearchClient {
      return instance ?: synchronized(this) {
        instance ?: VideoSearchClient().also { instance = it }
      }
    }

    fun getSearchSources(): List<VideoSearchSource> = VIDEO_SEARCH_SOURCES

    fun getSearchSourceByName(name: String): VideoSearchSource? = 
      VIDEO_SEARCH_SOURCES.find { it.name == name }
  }

  fun search(keyword: String, page: Int = 1): List<VideoSearchResult> {
    return searchWithSource(keyword, 0, page)
  }

  fun searchWithSource(
    keyword: String,
    sourceIndex: Int,
    page: Int = 1
  ): List<VideoSearchResult> {
    if (sourceIndex < 0 || sourceIndex >= VIDEO_SEARCH_SOURCES.size) {
      throw IllegalArgumentException("无效的搜索源索引: $sourceIndex")
    }

    val source = VIDEO_SEARCH_SOURCES[sourceIndex]
    return searchWithSource(keyword, source, page)
  }

  fun searchWithSource(
    keyword: String,
    sourceName: String,
    page: Int = 1
  ): List<VideoSearchResult> {
    val source = getSearchSourceByName(sourceName)
      ?: throw IllegalArgumentException("未找到搜索源: $sourceName")
    return searchWithSource(keyword, source, page)
  }

  private fun searchWithSource(
    keyword: String,
    source: VideoSearchSource,
    page: Int = 1
  ): List<VideoSearchResult> {
    if (!source.enabled) {
      throw RuntimeException("搜索源 ${source.name} 未启用")
    }

    val encodedKeyword = URLEncoder.encode(keyword, "UTF-8")
    val searchUrl = "${source.baseUrl}${source.searchPath}?q=$encodedKeyword&page=$page"

    return try {
      val html = fetchHtml(searchUrl)
      parseSearchResults(html, source.name)
    } catch (e: Exception) {
      println("${source.name} 搜索失败: ${e.message}")
      emptyList()
    }
  }

  private fun parseSearchResults(html: String, source: String): List<VideoSearchResult> {
    val results = mutableListOf<VideoSearchResult>()
    val titleRegex = Regex("<title>([^<]+)</title>")
    val titleMatches = titleRegex.findAll(html)

    titleMatches.forEach { match ->
      val title = match.groupValues[1]
      results.add(
        VideoSearchResult(
          title = title,
          year = null,
          rating = null,
          cover = null,
          description = "$source 搜索结果",
          type = "未知",
          source = source,
          playUrls = emptyList(),
          platform = source
        )
      )
    }

    return results
  }

  fun searchAllSources(keyword: String): Map<String, List<VideoSearchResult>> {
    return VIDEO_SEARCH_SOURCES.associate { source ->
      source.name to try {
        searchWithSource(keyword, source)
      } catch (e: Exception) {
        println("搜索源 ${source.name} 失败: ${e.message}")
        emptyList()
      }
    }
  }

  fun getVideoDetail(videoId: String, platform: String): VideoDetail? {
    return VideoDetail(
      title = "示例视频",
      year = "2024",
      rating = 8.5,
      cover = null,
      description = "视频详情",
      type = "电影",
      source = "搜索源",
      platform = platform,
      videoId = videoId,
      episodes = emptyList(),
      playUrls = emptyList()
    )
  }

  fun getPlayList(keyword: String): VideoPlayList {
    val results = search(keyword)
    val episodes = mutableListOf<VideoEpisode>()

    results.forEachIndexed { index, result ->
      episodes.add(
        VideoEpisode(
          episode = index + 1,
          title = result.title,
          url = "",
          platform = result.platform
        )
      )
    }

    return VideoPlayList(
      title = keyword,
      episodes = episodes,
      total = episodes.size
    )
  }

  fun downloadVideo(url: String, savePath: String): Boolean {
    return try {
      val request = Request.Builder()
        .url(url)
        .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
        .get()
        .build()

      client.newCall(request).execute().use { response ->
        if (response.isSuccessful) {
          val bytes = response.body?.bytes()
          if (bytes != null) {
            java.io.File(savePath).writeBytes(bytes)
            true
          } else {
            false
          }
        } else {
          false
        }
      }
    } catch (e: Exception) {
      println("下载失败: ${e.message}")
      false
    }
  }

  private fun fetchHtml(url: String): String {
    val request = Request.Builder()
      .url(url)
      .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
      .addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
      .addHeader("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8")
      .get()
      .build()

    client.newCall(request).execute().use { response ->
      val responseBody = response.body?.string()
        ?: throw RuntimeException("响应为空")

      if (!response.isSuccessful) {
        throw RuntimeException("请求失败: ${response.code}")
      }

      return responseBody
    }
  }
}
