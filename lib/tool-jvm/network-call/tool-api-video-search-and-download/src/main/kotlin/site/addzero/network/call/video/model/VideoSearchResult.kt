package site.addzero.network.call.video.model

data class VideoSearchResult(
  val video: VideoItem,
  val videos: List<VideoItem> = emptyList(),
  val page: Int = 1,
  val total: Int = 0,
  val hasMore: Boolean = false
)

data class VideoItem(
  val id: String,
  val title: String,
  val cover: String?,
  val year: String?,
  val type: VideoType,
  val platform: VideoPlatform,
  val rating: Double?,
  val playUrl: String?
)
}