package site.addzero.core.network.spi

interface HttpClientProfileSpi {
  val baseUrl: String
  val default: Boolean
    get() = false
  val enableCurlLogging: Boolean
    get() = true
  val headers: Map<String, String>
    get() = emptyMap()
  val token: String?
    get() = null
}
