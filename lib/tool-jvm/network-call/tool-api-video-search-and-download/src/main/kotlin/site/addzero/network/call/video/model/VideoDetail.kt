package site.addzero.network.call.video.model

data class VideoDetail(
  val id: String,
  val title: String,
  val cover: String?,
  val year: String?,
  val director: String?,
  val actor: String?,
  val area: String?,
  val content: String?,
  val playUrls: List<PlayUrl>,
  val episodes: List<VideoEpisode>,
  val type: VideoType,
  val platform: VideoPlatform,
  val rating: Double?,
  val updateTime: Long?
)

data class PlayUrl(
  val platform: String,
  val url: String,
  val quality: String
)

data class VideoEpisode(
  val episode: Int,
  val title: String,
  val url: String
)

data class VideoDownloadResult(
  val title: String,
  val videoUrl: String?,
  val imageUrl: String?,
  val size: Long,
  val progress: Int = 0,
  val filePath: String?,
  val status: DownloadStatus = DownloadStatus.PENDING
)

enum class DownloadStatus(val displayName: String) {
  PENDING("下载中"),
  RUNNING("下载中"),
  PAUSED("已暂停"),
  COMPLETED("已完成"),
  FAILED("下载失败"),
  CANCELLED("已取消")
}

enum class VideoPlatform(val displayName: String) {
  QQ("腾讯视频"),
  IQIYI("爱奇艺"),
  YOUKU("优酷"),
  MGTV("芒果TV"),
  BILIBILI("哔哩哔哩"),
  LETV("乐视"),
  TUDOU("土豆"),
  SOHU("搜狐视频"),
  M1905("1905电影网"),
  PPTV("PPTV"),
  WASU("华数TV"),
  ACFUN("AcFun")
}
