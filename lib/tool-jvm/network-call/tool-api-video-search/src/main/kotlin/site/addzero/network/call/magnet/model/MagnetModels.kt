package site.addzero.network.call.magnet.model

data class MagnetResult(
  val title: String,
  val magnetLink: String,
  val size: String,
  val date: String,
  val seeds: Int,
  val leeches: Int,
  val source: String,
  val hash: String? = null
)

data class MagnetSearchSource(
  val name: String,
  val baseUrl: String,
  val searchPath: String,
  val enabled: Boolean
)

data class MagnetSearchRequest(
  val keyword: String,
  val sourceName: String? = null,
  val sourceIndex: Int? = null,
  val page: Int = 1
)

data class MagnetSearchResponse(
  val success: Boolean,
  val results: List<MagnetResult>,
  val source: String,
  val keyword: String,
  val total: Int
)

data class MagnetInfo(
  val hash: String,
  val name: String,
  val size: Long,
  val files: List<MagnetFile>
)

data class MagnetFile(
  val path: String,
  val size: Long
)
