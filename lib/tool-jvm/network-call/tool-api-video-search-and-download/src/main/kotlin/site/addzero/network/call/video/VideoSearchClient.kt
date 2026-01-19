package site.addzero.network.call.video

import com.alibaba.fastjson2.JSON
import okhttp3.OkHttpClient
import okhttp3.Request
import site.addzero.ksp.singletonadapter.anno.SingletonAdapter
import site.addzero.network.call.video.model.*
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

@SingletonAdapter(singletonName = "VideoSearchUtil")
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
        type = VideoSearchType.MOVIE,
        enabled = true
      ),
      VideoSearchSource(
        name = "影视大全",
        baseUrl = "https://api.allvideo.com",
        searchPath = "/search",
        type = VideoSearchType.ALL,
        enabled = false
      ),
      VideoSearchSource(
        name = "QQ音乐",
        baseUrl = "https://api.y.qq.com",
        searchPath = "/search",
        type = VideoSearchType.MOVIE,
        enabled = false
      )
    )

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
    val searchUrl = "${source.baseUrl}${source.searchPath}?q=$encodedKeyword"

    return when (source.name) {
      "豆瓣" -> searchDouban(searchUrl)
      "影视大全" -> searchAllVideo(searchUrl)
      "QQ音乐" -> searchQQMusic(searchUrl)
      else -> throw IllegalArgumentException("不支持的搜索源: ${source.name}")
    }
  }

  private fun searchDouban(searchUrl: String): List<VideoSearchResult> {
    return try {
      val html = fetchHtml(searchUrl)
      val results = mutableListOf<VideoSearchResult>()
      
      val itemRegex = Regex("<a[^>]*class=\"item-root\"[^>]*>\\s*(<div[^>]*>\\s*</div>\\s*</a>")
      val itemMatches = itemRegex.findAll(html)
      
      itemMatches.forEach { match ->
        val itemHtml = match.groupValues[1]
        
        val titleRegex = Regex("class=\"item-root\"[^>]*>\\s*<[^>]*title\\s*</div>")
        val titleMatch = titleRegex.find(itemHtml)
        val title = titleMatch?.groupValues?.get(1)
        
        val ratingRegex = Regex("rating\\s*=\"([\\d.]+)\"")
        val ratingMatch = ratingRegex.find(itemHtml)
        val rating = ratingMatch?.groupValues?.get(1)?.toDoubleOrNull()
        
        val coverRegex = Regex("class=\"pic-img\"[^>]*>\\s*<img[^>]*>\\s*</img[^>]*src\\s*=\\s*\"")
        val coverMatch = coverRegex.find(itemHtml)
        val cover = coverMatch?.groupValues?.get(1)
        
        if (title != null) {
          results.add(
            VideoSearchResult(
              id = "",
              title = title,
              year = null,
              rating = rating,
              cover = cover,
              description = null,
              type = VideoType.MOVIE,
              source = "豆瓣",
              platform = "douban",
              playUrls = emptyList(),
              videoId = null
            )
          )
        }
      }
      
      results
    } catch (e: Exception) {
      println("豆瓣搜索失败: ${e.message}")
      emptyList()
    }
  }

  private fun searchAllVideo(searchUrl: String): List<VideoSearchResult> {
    return try {
      val html = fetchHtml(searchUrl)
      val results = mutableListOf<VideoSearchResult>()
      
      val videoRegex = Regex("<a[^>]*>\\s*class=\"video-item\"[^>]*>\\s*</div>\\s*</a>")
      val videoMatches = videoRegex.findAll(html)
      
      videoMatches.forEach { match ->
        val itemHtml = match.groupValues[1]
        
        val titleRegex = Regex("class=\"video-item\"[^>]*>\\s*<[^>]*>title\\s*</div>")
        val titleMatch = titleRegex.find(itemHtml)
        val title = titleMatch?.groupValues?.get(1)
        
        val ratingRegex = Regex("rating\\s*=\"([\\d.]+)\"")
        val ratingMatch = ratingRegex.find(itemHtml)
        val rating = ratingMatch?.groupValues?.get(1)?.toDoubleOrNull()
        
        val coverRegex = Regex("class=\"pic-img\"[^>]*>\\s*<img[^>]*>\\s*</img[^>]*src\\s*=\\s*\"")
        val coverMatch = coverRegex.find(itemHtml)
        val cover = coverMatch?.groupValues?.get(1)
        
        if (title != null) {
          results.add(
            VideoSearchResult(
              id = "",
              title = title,
              year = null,
              rating = rating,
              cover = cover,
              description = null,
              type = VideoType.MOVIE,
              source = "影视大全",
              platform = "allvideo",
              playUrls = emptyList(),
              videoId = null
            )
          )
        }
      }
      
      results
    } catch (e: Exception) {
      println("影视大全搜索失败: ${e.message}")
      emptyList()
    }
  }

  private fun searchQQMusic(searchUrl: String): List<VideoSearchResult> {
    return try {
      val html = fetchHtml(searchUrl)
      val results = mutableListOf<VideoSearchResult>()
      
      val songRegex = Regex("<a[^>]*>\\s*class=\"song-item\"[^>]*>\\s*</div>\\s*</a>")
      val songMatches = songRegex.findAll(html)
      
      songMatches.forEach { match ->
        val itemHtml = match.groupValues[1]
        
        val titleRegex = Regex("<div[^>]*>\\s*class=\"song-item\"[^>]*>\\s*<div[^>]*>title\\s*</div>")
        val titleMatch = titleRegex.find(itemHtml)
        val title = titleMatch?.groupValues?.get(1)
        
        val artistRegex = Regex("<div[^>]*>\\s*class=\"artist\"[^>]*>\\s*<div[^>]*>\\s*</div>")
        val artistMatch = artistRegex.find(itemHtml)
        val artist = artistMatch?.groupValues?.get(1)?.trim()
        
        val albumRegex = Regex("<div[^>]*>\\s*class=\"album\"[^>]*>\\s*<div[^>]*>\\s*</div>")
        val albumMatch = albumRegex.find(itemHtml)
        val album = albumMatch?.groupValues?.get(1)?.trim()
        
        if (title != null) {
          results.add(
            VideoSearchResult(
              id = "",
              title = title,
              year = null,
              rating = null,
              cover = null,
              description = null,
              type = VideoType.MOVIE,
              source = "QQ音乐",
              platform = "qq.com",
              playUrls = emptyList(),
              videoId = null
            )
          )
        }
      }
      
      results
    } catch (e: Exception) {
      println("QQ音乐搜索失败: ${e.message}")
      emptyList()
    }
  }

  fun getVideoDetail(videoId: String, platform: VideoPlatform): VideoDetail? {
    val result = when (platform) {
      VideoPlatform.QQ -> getQQVideoDetail(videoId)
      VideoPlatform.IQIYI -> getIQIYIVideoDetail(videoId)
      else -> null
    }
    
    return result
  }

  private fun getQQVideoDetail(videoId: String): VideoDetail? {
    return try {
      val apiUrl = "https://api.y.qq.com/x/v1/mobile/v2/rest/video/info?video_ids=[$videoId]"
      val response = fetchJson(apiUrl)
      
      val json = response.getJSONObject("data")
      
      val videoItems = json.getJSONArray("list")
      val video = videoItems.getJSONObject(0)
      
      val videoDetail = VideoDetail(
        id = video.getString("videoId"),
        title = video.getString("title"),
        year = video.getString("year")?.toIntOrNull(),
        director = null,
        actor = null,
        area = null,
        content = null,
        type = VideoType.MOVIE,
        source = "腾讯视频",
        platform = "qq.com",
        videoId = videoId,
        episodes = emptyList(),
        playUrls = emptyList()
      )
      
      return videoDetail
    } catch (e: Exception) {
      println("获取腾讯视频详情失败: ${e.message}")
      null
    }
  }

  private fun getIQIYIVideoDetail(videoId: String): VideoDetail? {
    return try {
      val apiUrl = "https://www.iqiyi.com/drama/series/$videoId"
      val response = fetchHtml(apiUrl)
      val results = mutableListOf<VideoDetail>()
      
      val titleRegex = Regex("class=\"title\"[^>]*>\\s*<[^>]*>\\s*</div>\\s*</div>\\s*</div>\\s*</a>")
      val titleMatch = titleRegex.find(response)
      val title = titleMatch?.groupValues?.get(1)?.trim()
      
      val infoList = response.split("播放源").map { it.trim() }
      
      val playUrls = infoList.filter { it.isNotBlank() }.map { url ->
        PlayUrl("腾讯视频", url, "高清")
      }
      
      results.add(
        VideoDetail(
          id = videoId,
          title = title ?: "",
          year = null,
          director = null,
          actor = null,
          area = null,
          content = null,
          type = VideoType.TV_SERIES,
          source = "爱奇艺",
          platform = "iqiyi.com",
          videoId = videoId,
          episodes = emptyList(),
          playUrls = playUrls
        )
      )
      
      return results.firstOrNull()
    } catch (e: Exception) {
      println("获取爱奇艺视频详情失败: ${e.message}")
      null
    }
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

  private fun fetchJson(url: String): JSONObject {
    val html = fetchHtml(url)
    val jsonRegex = Regex("<script\\s*>window\\s*=\\s*var\\s*response\\s*=\\s*{.*?}\\s*</script>")
    val jsonMatch = jsonRegex.find(html)
    
    return if (jsonMatch != null) {
      try {
        val jsonString = jsonMatch.groupValues.get(1).trim()
        JSON.parseObject(jsonString) as JSONObject
      } catch (e: Exception) {
        println("解析 JSON 失败: ${e.message}")
        JSONObject()
      }
    } else {
      JSONObject()
    }
  }

  fun searchByPlatform(keyword: String, platform: VideoPlatform): List<VideoSearchResult> {
    return when (platform) {
      VideoPlatform.QQ -> searchQQMusic(keyword)
      VideoPlatform.IQIYI -> searchIQIYIMusic(keyword)
      else -> throw IllegalArgumentException("不支持的平台: ${platform.displayName}")
    }
  }

  fun searchByType(keyword: String, type: VideoType): List<VideoSearchResult> {
    return VIDEO_SEARCH_SOURCES
      .filter { it.type == type && it.enabled }
      .mapNotNull { source ->
        try {
          searchWithSource(keyword, source)
        } catch (e: Exception) {
          emptyList()
        }
      }
      .flatten()
  }
}
