package site.addzero.network.call.video.model

data class VideoSearchResult(
  val title: String,
  val year: String?,
  val rating: Double?,
  val cover: String?,
  val description: String?,
  val type: String,
  val source: String,
  val playUrls: List<PlayUrl>,
  val platform: String,
  val videoId: String? = null
)

data class VideoSearchSource(
  val name: String,
  val baseUrl: String,
  val searchPath: String,
  val type: String?,
  val enabled: Boolean
)

data class VideoDetail(
  val title: String,
  val year: String?,
  val rating: Double?,
  val cover: String?,
  val description: String?,
  val type: String,
  val source: String,
  val platform: String,
  val videoId: String?,
  val episodes: List<VideoEpisode>,
  val playUrls: List<PlayUrl>
)

data class VideoPlayList(
  val title: String,
  val episodes: List<VideoEpisode>,
  val total: Int
)

data class VideoEpisode(
  val episode: Int,
  val title: String,
  val url: String,
  val platform: String
)

data class PlayUrl(
  val platform: String,
  val url: String,
  val quality: String
)
