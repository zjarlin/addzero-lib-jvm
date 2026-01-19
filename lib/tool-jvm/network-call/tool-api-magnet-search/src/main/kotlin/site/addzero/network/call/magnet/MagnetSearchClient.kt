package site.addzero.network.call.magnet

import okhttp3.OkHttpClient
import okhttp3.Request
import site.addzero.network.call.magnet.model.*
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

class MagnetSearchClient(
  private val baseUrl: String = "https://btdig.com"
) {

  private val client = OkHttpClient.Builder()
    .connectTimeout(10, TimeUnit.SECONDS)
    .readTimeout(30, TimeUnit.SECONDS)
    .build()

  companion object {
    @Volatile
    private var instance: MagnetSearchClient? = null
    
    private var sourcesCache: List<MagnetSearchSource>? = null

    fun getInstance(): MagnetSearchClient {
      return instance ?: synchronized(this) {
        instance ?: MagnetSearchClient().also { instance = it }
      }
    }
    
    private val MAGNET_SEARCH_SOURCES by lazy {
      listOf(
        MagnetSearchSource(
          name = "BTDig",
          baseUrl = "https://btdig.com",
          searchPath = "/q?q=",
          enabled = true
        ),
        MagnetSearchSource(
          name = "BTDB",
          baseUrl = "https://btdb.to",
          searchPath = "/q?q=",
          enabled = true
        ),
        MagnetSearchSource(
          name = "BTSOW",
          baseUrl = "https://btsow.club",
          searchPath = "/search?q=",
          enabled = true
        ),
        MagnetSearchSource(
          name = "TorrentProject",
          baseUrl = "https://torrentproject2.com",
          searchPath = "/?t=",
          enabled = false
        ),
        MagnetSearchSource(
          name = "MagnetDL",
          baseUrl = "https://www.magnetdl.com",
          searchPath = "/?q=",
          enabled = false
        )
      )
    }
  }

  fun getSearchSources(): List<MagnetSearchSource> = MAGNET_SEARCH_SOURCES

  fun getSearchSourceByName(name: String): MagnetSearchSource? = 
      MAGNET_SEARCH_SOURCES.find { it.name == name }

  fun search(keyword: String, page: Int = 1): List<MagnetResult> {
    return searchWithSource(keyword, 0, page)
  }

  fun searchWithSource(
    keyword: String,
    sourceIndex: Int,
    page: Int = 1
  ): List<MagnetResult> {
    if (sourceIndex < 0 || sourceIndex >= MAGNET_SEARCH_SOURCES.size) {
      throw IllegalArgumentException("无效的搜索源索引: $sourceIndex")
    }

    val source = MAGNET_SEARCH_SOURCES[sourceIndex]
    return searchWithSource(keyword, source, page)
  }

  fun searchWithSource(
    keyword: String,
    sourceName: String,
    page: Int = 1
  ): List<MagnetResult> {
    val source = getSearchSourceByName(sourceName)
      ?: throw IllegalArgumentException("未找到搜索源: $sourceName")
    return searchWithSource(keyword, source, page)
  }

  private fun searchWithSource(
    keyword: String,
    source: MagnetSearchSource,
    page: Int = 1
  ): List<MagnetResult> {
    if (!source.enabled) {
      throw RuntimeException("搜索源 ${source.name} 未启用")
    }

    val encodedKeyword = URLEncoder.encode(keyword, "UTF-8")
    val searchUrl = "${source.baseUrl}${source.searchPath}$encodedKeyword"

    return try {
      val html = fetchHtml(searchUrl)
      val results = mutableListOf<MagnetResult>()
      
      val magnetRegex = Regex("href=\"(magnet:[^\"]+)\"")
      val magnetMatches = magnetRegex.findAll(html)
      
      val titleRegex = Regex("title=\"([^\"]+)\"")
      val titleMatches = titleRegex.findAll(html)
      
      val magnetLinks = magnetMatches.map { it.groupValues.get(0) }.toList()
      val titles = titleMatches.map { it.groupValues.get(0) }.toList()
      
      val count = minOf(magnetLinks.size, titles.size)
      for (i in 0 until count) {
        results.add(
          MagnetResult(
            title = titles[i],
            magnetLink = magnetLinks[i],
            size = "Unknown",
            date = "Unknown",
            seeds = 0,
            leeches = 0,
            source = source.name
          )
        )
      }
      
      results
    } catch (e: Exception) {
      println("${source.name} 搜索失败: ${e.message}")
      emptyList()
    }
  }

  fun searchAllSources(keyword: String): Map<String, List<MagnetResult>> {
    return MAGNET_SEARCH_SOURCES.associate { source ->
      source.name to try {
        searchWithSource(keyword, source)
      } catch (e: Exception) {
        println("搜索源 ${source.name} 失败: ${e.message}")
        emptyList()
      }
    }
  }

  fun getMagnetHash(magnetLink: String): String? {
    val hashRegex = Regex("urn:btih:([a-fA-F0-9]{40})", RegexOption.IGNORE_CASE)
    val match = hashRegex.find(magnetLink)
    if (match != null) {
      return match.groupValues[1]
    }
    
    val hashRegexShort = Regex("urn:btih:([a-fA-F0-9]{32,40})", RegexOption.IGNORE_CASE)
    val matchShort = hashRegexShort.find(magnetLink)
    return matchShort?.groupValues?.get(1)
  }

  fun validateMagnetLink(magnetLink: String): Boolean {
    return magnetLink.startsWith("magnet:?xt=urn:btih:") && 
           getMagnetHash(magnetLink) != null
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

  fun createMagnetLink(hash: String, displayName: String): String {
    val encodedName = URLEncoder.encode(displayName, "UTF-8")
    return "magnet:?xt=urn:btih:$hash&dn=$encodedName"
  }

  fun searchMagnetHash(hash: String): List<MagnetResult> {
    return search(hash)
  }
}
