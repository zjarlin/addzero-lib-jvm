package site.addzero.network.call.video.tencent

import com.alibaba.fastjson2.JSON
import com.alibaba.fastjson2.JSONArray
import com.alibaba.fastjson2.JSONObject
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

/**
 * 腾讯视频搜索客户端
 *
 * 使用腾讯视频官方搜索接口进行视频搜索
 */
class TencentVideoSearchClient {

  private val client = OkHttpClient.Builder()
    .connectTimeout(15, TimeUnit.SECONDS)
    .readTimeout(30, TimeUnit.SECONDS)
    .build()

  companion object {
    private const val SEARCH_API = "https://node.video.qq.com/x/api/search"
    private const val DEFAULT_PAGE_SIZE = 20

    @Volatile
    private var instance: TencentVideoSearchClient? = null

    fun getInstance(): TencentVideoSearchClient {
      return instance ?: synchronized(this) {
        instance ?: TencentVideoSearchClient().also { instance = it }
      }
    }
  }

  /**
   * 搜索视频
   * @param keyword 搜索关键词
   * @param page 页码，从0开始
   * @param pageSize 每页数量
   * @return 搜索结果
   */
  fun search(
    keyword: String,
    page: Int = 0,
    pageSize: Int = DEFAULT_PAGE_SIZE
  ): TencentSearchResult {
    val encodedKeyword = URLEncoder.encode(keyword, "UTF-8")
    val url = "$SEARCH_API?query=$encodedKeyword&pagenum=$page&pagesize=$pageSize"

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
        throw RuntimeException("搜索请求失败: ${response.code}")
      }

      val body = response.body?.string() ?: throw RuntimeException("响应为空")
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
   */
  fun buildParseUrl(playUrl: String, parseBaseUrl: String = "https://jx.jsonplayer.com/player/?url="): String {
    val encodedUrl = URLEncoder.encode(playUrl, "UTF-8")
    return "$parseBaseUrl$encodedUrl"
  }
}

/**
 * 腾讯视频搜索结果
 */
data class TencentSearchResult(
  val keyword: String,
  val page: Int,
  val total: Int,
  val hasMore: Boolean,
  val items: List<TencentVideoItem>
)

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
}
