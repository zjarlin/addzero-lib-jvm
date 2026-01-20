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

