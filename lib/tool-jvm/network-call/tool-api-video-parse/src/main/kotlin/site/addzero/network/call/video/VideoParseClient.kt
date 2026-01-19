package site.addzero.network.call.video

import com.alibaba.fastjson2.JSON
import com.alibaba.fastjson2.JSONObject
import okhttp3.OkHttpClient
import okhttp3.Request
import site.addzero.network.call.video.model.*
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

class VideoParseClient(
  private val baseUrl: String = "https://api.example.com/video"
) {

  private val client = OkHttpClient.Builder()
    .connectTimeout(10, TimeUnit.SECONDS)
    .readTimeout(30, TimeUnit.SECONDS)
    .build()

  companion object {
    private val VIDEO_PARSE_SOURCES = listOf(
      ParseSource("综合", ParseType.IFRAME_POPUP, "https://jx.jsonplayer.com/player/?url="),
      ParseSource("CK", ParseType.IFRAME_POPUP, "https://www.ckplayer.vip/jiexi/?url="),
      ParseSource("YT", ParseType.IFRAME_POPUP, "https://jx.yangtu.top/?url="),
      ParseSource("Player-JY", ParseType.IFRAME_POPUP, "https://jx.playerjy.com/?url="),
      ParseSource("yparse", ParseType.IFRAME_POPUP_WITH_EPISODE, "https://jx.yparse.com/index.php?url="),
      ParseSource("8090", ParseType.IFRAME_POPUP, "https://www.8090g.cn/?url="),
      ParseSource("剖元", ParseType.IFRAME_POPUP, "https://www.pouyun.com/?url="),
      ParseSource("虾米", ParseType.IFRAME_POPUP, "https://jx.xmflv.com/?url="),
      ParseSource("全民", ParseType.IFRAME_POPUP, "https://43.240.74.102.4433?url="),
      ParseSource("171", ParseType.IFRAME_POPUP, "https://api.171k.pw/?url="),
      ParseSource("OK", ParseType.IFRAME_POPUP, "https://api.okjx.com/?url="),
      ParseSource("BL", ParseType.IFRAME_POPUP, "https://api.bljiex.com/?url="),
      ParseSource("m3u8", ParseType.IFRAME_POPUP, "https://jx.m3u8.tv/?url="),
      ParseSource("hls", ParseType.IFRAME_POPUP, "https://jx.hls.vipijiexi.com/?url="),
      ParseSource("jsonplayer", ParseType.IFRAME_POPUP, "https://jx.jsonplayer.com/?url=")
    )

    @Volatile
    private var instance: VideoParseClient? = null

    fun getInstance(): VideoParseClient {
      return instance ?: synchronized(this) {
        instance ?: VideoParseClient().also { instance = it }
      }
    }

    fun getParseSources(): List<ParseSource> = VIDEO_PARSE_SOURCES

    fun getParseSourceByName(name: String): ParseSource? = 
      VIDEO_PARSE_SOURCES.find { it.name == name }
  }

  fun parse(url: String): VideoParseResult? {
    return parseWithSource(url, 0)
  }

  fun parseWithSource(url: String, sourceIndex: Int): VideoParseResult? {
    if (sourceIndex < 0 || sourceIndex >= VIDEO_PARSE_SOURCES.size) {
      throw IllegalArgumentException("无效的解析源索引: $sourceIndex")
    }

    val source = VIDEO_PARSE_SOURCES[sourceIndex]
    return parseWithSource(url, source)
  }

  fun parseWithSource(url: String, sourceName: String): VideoParseResult? {
    val source = getParseSourceByName(sourceName)
      ?: throw IllegalArgumentException("未找到解析源: $sourceName")
    return parseWithSource(url, source)
  }

  fun parseWithSource(url: String, source: ParseSource): VideoParseResult? {
    val encodedUrl = URLEncoder.encode(url, "UTF-8")
    val parseUrl = source.url + encodedUrl

    when (source.type) {
      ParseType.IFRAME_EMBEDDED -> {
        return getIframeParseResult(parseUrl, url, source.name)
      }
      ParseType.IFRAME_POPUP, ParseType.IFRAME_POPUP_WITH_EPISODE, ParseType.IFRAME_POPUP_WITHOUT_EPISODE -> {
        return getIframeParseResult(parseUrl, url, source.name)
      }
    }
  }

  private fun getIframeParseResult(parseUrl: String, originalUrl: String, sourceName: String): VideoParseResult? {
    try {
      val html = fetchHtml(parseUrl)
      
      val title = extractTitle(html) ?: "未知视频"
      val iframeUrl = parseUrl

      return VideoParseResult(
        title = title,
        author = null,
        authorId = null,
        cover = null,
        duration = null,
        videoUrls = listOf(iframeUrl),
        audioUrls = emptyList(),
        description = "使用 $sourceName 解析源",
        likes = null,
        comments = null,
        shares = null,
        playCount = null,
        createTime = System.currentTimeMillis(),
        parseSource = sourceName,
        originalUrl = originalUrl
      )
    } catch (e: Exception) {
      println("解析失败: ${e.message}")
      throw RuntimeException("解析失败: ${e.message}", e)
    }
  }

  fun parseAllSources(url: String): Map<String, VideoParseResult?> {
    return VIDEO_PARSE_SOURCES.associate { source ->
      source.name to try {
        parseWithSource(url, source)
      } catch (e: Exception) {
        println("解析源 ${source.name} 失败: ${e.message}")
        null
      }
    }
  }

  private fun fetchHtml(url: String): String {
    val request = Request.Builder()
      .url(url)
      .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
      .addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8")
      .addHeader("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8")
      .addHeader("Referer", extractDomain(url))
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

  fun detectPlatform(url: String): VideoPlatform {
    return when {
      url.contains("v.qq.com") || url.contains("m.v.qq.com") -> VideoPlatform.QQ
      url.contains("iqiyi.com") || url.contains("m.iqiyi.com") || url.contains("iq.com") -> VideoPlatform.IQIYI
      url.contains("youku.com") || url.contains("m.youku.com") -> VideoPlatform.YOUKU
      url.contains("mgtv.com") -> VideoPlatform.MGTV
      url.contains("bilibili.com") || url.contains("m.bilibili.com") -> VideoPlatform.BILIBILI
      url.contains("le.com") -> VideoPlatform.LETV
      url.contains("tudou.com") -> VideoPlatform.TUDOU
      url.contains("sohu.com") -> VideoPlatform.SOHU
      url.contains("1905.com") -> VideoPlatform.M1905
      url.contains("pptv.com") -> VideoPlatform.PPTV
      url.contains("wasu.cn") -> VideoPlatform.WASU
      url.contains("acfun.cn") -> VideoPlatform.ACFUN
      else -> VideoPlatform.UNKNOWN
    }
  }

  fun getParseUrl(url: String, sourceIndex: Int = 0): String {
    if (sourceIndex < 0 || sourceIndex >= VIDEO_PARSE_SOURCES.size) {
      throw IllegalArgumentException("无效的解析源索引: $sourceIndex")
    }

    val source = VIDEO_PARSE_SOURCES[sourceIndex]
    val encodedUrl = URLEncoder.encode(url, "UTF-8")
    return source.url + encodedUrl
  }

  fun getParseUrl(url: String, sourceName: String): String {
    val source = getParseSourceByName(sourceName)
      ?: throw IllegalArgumentException("未找到解析源: $sourceName")
    
    val encodedUrl = URLEncoder.encode(url, "UTF-8")
    return source.url + encodedUrl
  }

  private fun extractTitle(html: String): String? {
    val titleRegex = Regex("<title>(.*?)</title>", RegexOption.IGNORE_CASE)
    return titleRegex.find(html)?.groupValues?.get(1)?.trim()
      ?: Regex("title\\s*:\\s*[\"'](.*?)[\"']", RegexOption.IGNORE_CASE)
        .find(html)?.groupValues?.get(1)?.trim()
  }

  private fun extractDomain(url: String): String {
    val uri = java.net.URI(url)
    return "${uri.scheme}://${uri.host}"
  }

  fun isSupportedUrl(url: String): Boolean {
    val platform = detectPlatform(url)
    return platform != VideoPlatform.UNKNOWN
  }

  fun getVideoInfo(url: String): VideoInfo? {
    val platform = detectPlatform(url)
    val parseUrl = getParseUrl(url, 0)

    return VideoInfo(
      title = "视频信息",
      author = null,
      authorId = null,
      cover = null,
      duration = null,
      description = "平台: ${platform.displayName}",
      likes = null,
      comments = null,
      shares = null,
      playCount = null,
      createTime = System.currentTimeMillis(),
      platform = platform.displayName,
      videoId = extractVideoId(url, platform),
      parseUrl = parseUrl,
      sourceName = VIDEO_PARSE_SOURCES[0].name
    )
  }

  private fun extractVideoId(url: String, platform: VideoPlatform): String? {
    return when (platform) {
      VideoPlatform.BILIBILI -> {
        val bvidRegex = Regex("BV[\\w]{10}")
        bvidRegex.find(url)?.value
      }
      else -> {
        val idRegex = Regex("[?&]id=([^&]+)")
        idRegex.find(url)?.groupValues?.get(1)
          ?: url.substringAfterLast("/").takeIf { it.isNotEmpty() }
      }
    }
  }
}
