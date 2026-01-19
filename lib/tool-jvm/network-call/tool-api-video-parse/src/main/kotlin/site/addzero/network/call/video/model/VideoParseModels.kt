package site.addzero.network.call.video.model

data class VideoParseResult(
  val title: String,
  val author: String?,
  val authorId: Long?,
  val cover: String?,
  val duration: Long?,
  val videoUrls: List<String>,
  val audioUrls: List<String>,
  val description: String?,
  val likes: Long?,
  val comments: Long?,
  val shares: Long?,
  val playCount: Long?,
  val createTime: Long?,
  val parseSource: String? = null,
  val originalUrl: String? = null
)

data class VideoInfo(
  val title: String,
  val author: String?,
  val authorId: Long?,
  val cover: String?,
  val duration: Long?,
  val description: String?,
  val likes: Long?,
  val comments: Long?,
  val shares: Long?,
  val playCount: Long?,
  val createTime: Long?,
  val platform: String?,
  val videoId: String?,
  val parseUrl: String? = null,
  val sourceName: String? = null
)

data class VideoParseRequest(
  val url: String,
  val platform: Platform? = null,
  val sourceIndex: Int? = null,
  val sourceName: String? = null
)

data class VideoParseResponse(
  val code: Int,
  val message: String?,
  val data: VideoParseResult?
)

data class ParseSource(
  val name: String,
  val type: ParseType,
  val url: String
)

enum class Platform(val value: String) {
  DOUYIN("douyin"),
  KUAISHOU("kuaishou"),
  BILIBILI("bilibili"),
  WEIBO("weibo"),
  XIAOHONGSHU("xiaohongshu"),
  UNKNOWN("unknown")
}

enum class ParseType(val displayName: String) {
  IFRAME_EMBEDDED("内嵌播放"),
  IFRAME_POPUP("弹窗播放带选集"),
  IFRAME_POPUP_WITH_EPISODE("弹窗播放不带选集"),
  IFRAME_POPUP_WITHOUT_EPISODE("内嵌播放")
}

enum class VideoPlatform(val displayName: String, val host: String) {
  QQ("腾讯视频", "v.qq.com"),
  IQIYI("爱奇艺", "iqiyi.com"),
  YOUKU("优酷", "youku.com"),
  MGTV("芒果TV", "mgtv.com"),
  BILIBILI("哔哩哔哩", "bilibili.com"),
  LETV("乐视", "le.com"),
  TUDOU("土豆", "tudou.com"),
  SOHU("搜狐视频", "sohu.com"),
  M1905("1905电影网", "1905.com"),
  PPTV("PPTV", "pptv.com"),
  WASU("华数TV", "wasu.cn"),
  ACFUN("AcFun", "acfun.cn"),
  UNKNOWN("未知平台", "unknown")
}
