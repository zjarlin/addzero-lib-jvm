package site.addzero.network.call.video.model

data class PlayUrl(
  val platform: String,
  val url: String,
  val quality: String
) {
  companion object {
    fun of(platform: VideoPlatform): PlayUrl {
      return PlayUrl(platform.displayName, "", "")
    }
  }
}
