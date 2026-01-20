package site.addzero.network.call.video.tencent

import com.alibaba.fastjson2.JSON
import com.alibaba.fastjson2.JSONObject
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

/**
 * 腾讯视频搜索客户端
 *
 * 提供视频搜索和解析URL生成功能
 *
 * 注意：腾讯视频搜索API可能需要签名验证，如果搜索不可用，
 * 可以直接使用 buildParseUrl 方法配合手动获取的播放地址
 */
class TencentVideoSearchClient {

  private val client = OkHttpClient.Builder()
    .connectTimeout(15, TimeUnit.SECONDS)
    .readTimeout(30, TimeUnit.SECONDS)
    .followRedirects(true)
    .build()

  companion object {
    // 备用搜索API列表（按优先级排序）
    private val SEARCH_APIS = listOf(
      "https://node.video.qq.com/x/api/search",
      "https://pbaccess.video.qq.com/trpc.videosearch.search_cgi.http/MbSearch"
    )
    private const val DEFAULT_PAGE_SIZE = 20

    @Volatile
    private var instance: TencentVideoSearchClient? = null

    fun getInstance(): TencentVideoSearchClient {
      return instance ?: synchronized(this) {
        instance ?: TencentVideoSearchClient().also { instance = it }
      }
    }

    /**
     * 预定义的解析源列表
     */
    val PARSE_SOURCES = listOf(
      ParseSource("综合", "https://jx.jsonplayer.com/player/?url="),
      ParseSource("CK", "https://www.ckplayer.vip/jiexi/?url="),
      ParseSource("YT", "https://jx.yangtu.top/?url="),
      ParseSource("Player-JY", "https://jx.playerjy.com/?url="),
      ParseSource("yparse", "https://jx.yparse.com/index.php?url="),
      ParseSource("8090", "https://www.8090g.cn/?url="),
      ParseSource("剖元", "https://www.pouyun.com/?url="),
      ParseSource("虾米", "https://jx.xmflv.com/?url="),
      ParseSource("OK", "https://api.okjx.com/?url="),
      ParseSource("BL", "https://api.bljiex.com/?url="),
      ParseSource("m3u8", "https://jx.m3u8.tv/?url=")
    )
  }

  /**
   * 搜索视频
   * @param keyword 搜索关键词
   * @param page 页码，从0开始
   * @param pageSize 每页数量
   * @return 搜索结果，如果搜索失败返回空结果
   */
  fun search(
    keyword: String,
    page: Int = 0,
    pageSize: Int = DEFAULT_PAGE_SIZE
  ): TencentSearchResult {
    val encodedKeyword = URLEncoder.encode(keyword, "UTF-8")

    // 尝试多个API
    for (apiBase in SEARCH_APIS) {
      try {
        val url = "$apiBase?query=$encodedKeyword&pagenum=$page&pagesize=$pageSize&version=2&platform=2"
        val result = trySearchApi(url, keyword, page)
        if (result.items.isNotEmpty()) {
          return result
        }
      } catch (e: Exception) {
        // 继续尝试下一个API
        continue
      }
    }

    // 所有API都失败，返回空结果
    return TencentSearchResult(
      keyword = keyword,
      page = page,
      total = 0,
      hasMore = false,
      items = emptyList(),
      error = "搜索API暂不可用，请直接使用播放地址"
    )
  }

  private fun trySearchApi(url: String, keyword: String, page: Int): TencentSearchResult {
    val request = Request.Builder()
      .url(url)
      .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
      .addHeader("Accept", "application/json, text/plain, */*")
      .addHeader("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8")
      .addHeader("Referer", "https://v.qq.com/")
      .addHeader("Origin", "https://v.qq.com")
      .get()
      .build()

    client.newCall(request).execute().use { response ->
      if (!response.isSuccessful) {
        return TencentSearchResult(keyword = keyword, page = page, total = 0, hasMore = false, items = emptyList())
      }

      val body = response.body?.string() ?: return TencentSearchResult(keyword = keyword, page = page, total = 0, hasMore = false, items = emptyList())
      return parseSearchResult(body, keyword, page)
    }
  }

  private fun parseSearchResult(json: String, keyword: String, page: Int): TencentSearchResult {
    val jsonObj = JSON.parseObject(json)
    val resultList = mutableListOf<TencentVideoItem>()

    // 尝试解析不同的数据结构
    val data = jsonObj.getJSONObject("data")
    if (data != null) {
      // 解析搜索结果列表
      val normalList = data.getJSONArray("normalList")
      val areaBoxList = data.getJSONArray("areaBoxList")

      normalList?.forEach { item ->
        parseVideoItem(item as JSONObject)?.let { resultList.add(it) }
      }

      areaBoxList?.forEach { areaBox ->
        val box = areaBox as JSONObject
        val itemList = box.getJSONArray("itemList")
        itemList?.forEach { item ->
          parseVideoItem(item as JSONObject)?.let { resultList.add(it) }
        }
      }
    }

    // 备用解析: 尝试解析uiData
    if (resultList.isEmpty()) {
      val uiData = jsonObj.getJSONArray("uiData") ?: jsonObj.getJSONArray("results")
      uiData?.forEach { item ->
        parseVideoItem(item as JSONObject)?.let { resultList.add(it) }
      }
    }

    return TencentSearchResult(
      keyword = keyword,
      page = page,
      total = resultList.size,
      hasMore = resultList.size >= DEFAULT_PAGE_SIZE,
      items = resultList
    )
  }

  private fun parseVideoItem(json: JSONObject): TencentVideoItem? {
    // 获取视频ID (多种可能的字段名)
    val videoId = json.getString("id")
      ?: json.getString("cid")
      ?: json.getString("vid")
      ?: return null

    // 获取标题
    val title = json.getString("title")
      ?: json.getString("typeName")
      ?: "未知标题"

    // 获取封面
    val cover = json.getString("pic")
      ?: json.getString("coverUrl")
      ?: json.getString("horizontal_pic_url")

    // 获取播放链接
    val playUrl = json.getString("playUrl")
      ?: json.getString("url")
      ?: buildPlayUrl(videoId)

    // 获取类型
    val typeName = json.getString("typeName")
      ?: json.getString("videoType")
      ?: ""

    // 获取评分
    val score = json.getString("score")?.toDoubleOrNull()

    // 获取简介
    val description = json.getString("description")
      ?: json.getString("secondTitle")

    // 获取年份
    val year = json.getString("year")

    // 获取演员
    val actors = json.getString("mainActor")
      ?: json.getJSONArray("leadingActor")?.joinToString(", ")

    // 获取更新信息
    val updateInfo = json.getString("timeLong")
      ?: json.getString("episode")
      ?: json.getString("publish_date")

    return TencentVideoItem(
      id = videoId,
      title = cleanHtmlTags(title),
      cover = cover,
      playUrl = playUrl,
      typeName = typeName,
      score = score,
      description = description?.let { cleanHtmlTags(it) },
      year = year,
      actors = actors,
      updateInfo = updateInfo
    )
  }

  private fun buildPlayUrl(videoId: String): String {
    return "https://v.qq.com/x/cover/$videoId.html"
  }

  private fun cleanHtmlTags(text: String): String {
    return text
      .replace(Regex("<[^>]+>"), "")
      .replace("&nbsp;", " ")
      .replace("&amp;", "&")
      .replace("&lt;", "<")
      .replace("&gt;", ">")
      .replace("&quot;", "\"")
      .trim()
  }

  /**
   * 获取视频详情页URL
   */
  fun getVideoDetailUrl(videoId: String): String {
    return "https://v.qq.com/x/cover/$videoId.html"
  }

  /**
   * 构建解析URL（配合视频解析接口使用）
   * 这是核心功能，即使搜索API不可用，也可以直接使用此方法
   *
   * @param playUrl 视频播放页URL（如 https://v.qq.com/x/cover/xxx.html）
   * @param parseBaseUrl 解析源基础URL，默认使用综合解析源
   * @return 完整的解析URL
   */
  fun buildParseUrl(playUrl: String, parseBaseUrl: String = "https://jx.jsonplayer.com/player/?url="): String {
    val encodedUrl = URLEncoder.encode(playUrl, "UTF-8")
    return "$parseBaseUrl$encodedUrl"
  }

  /**
   * 使用指定解析源构建解析URL
   *
   * @param playUrl 视频播放页URL
   * @param sourceName 解析源名称（如"综合"、"CK"等）
   * @return 完整的解析URL
   */
  fun buildParseUrlBySource(playUrl: String, sourceName: String): String {
    val source = PARSE_SOURCES.find { it.name == sourceName } ?: PARSE_SOURCES.first()
    return buildParseUrl(playUrl, source.url)
  }

  /**
   * 获取所有解析源的URL
   *
   * @param playUrl 视频播放页URL
   * @return 解析源名称到解析URL的映射
   */
  fun getAllParseUrls(playUrl: String): Map<String, String> {
    return PARSE_SOURCES.associate { source ->
      source.name to buildParseUrl(playUrl, source.url)
    }
  }
}

/**
 * 解析源
 */
data class ParseSource(
  val name: String,
  val url: String
)

/**
 * 腾讯视频搜索结果
 */
data class TencentSearchResult(
  val keyword: String,
  val page: Int,
  val total: Int,
  val hasMore: Boolean,
  val items: List<TencentVideoItem>,
  val error: String? = null
) {
  val isSuccess: Boolean get() = error == null && items.isNotEmpty()
}

/**
 * 腾讯视频搜索项
 */
data class TencentVideoItem(
  val id: String,
  val title: String,
  val cover: String?,
  val playUrl: String,
  val typeName: String,
  val score: Double?,
  val description: String?,
  val year: String?,
  val actors: String?,
  val updateInfo: String?
) {
  /**
   * 获取解析播放地址
   */
  fun getParseUrl(parseBaseUrl: String = "https://jx.jsonplayer.com/player/?url="): String {
    val encodedUrl = URLEncoder.encode(playUrl, "UTF-8")
    return "$parseBaseUrl$encodedUrl"
  }

  /**
   * 获取所有解析源的URL
   */
  fun getAllParseUrls(): Map<String, String> {
    return TencentVideoSearchClient.PARSE_SOURCES.associate { source ->
      source.name to getParseUrl(source.url)
    }
  }
}
