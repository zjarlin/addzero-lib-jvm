package site.addzero.network.call.lsky.model

data class LskyUploadResult(
  val success: Boolean,
  val message: String?,
  val data: LskyUploadData?
)

data class LskyUploadData(
  val url: String,
  val filename: String,
  val extension: String,
  val size: Long,
  val mime: String,
  val strategy_id: Int,
  val created_at: String,
  val updated_at: String
)

data class LskyUploadConfig(
  val baseUrl: String = "https://lsky.zhongzhuan.chat",
  val uploadEndpoint: String = "/upload",
  val strategyId: Int = 1,
  val xsrfToken: String? = null,
  val sessionId: String? = null,
  val csrfToken: String? = null
)