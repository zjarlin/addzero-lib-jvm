package site.addzero.core.network.spi

interface HttpClientProfileSpi {
  val baseUrl: String
  val default
    get() = false
  val enableCurlLogging
    get() = true
  val headers: Map<String, String>
    get() = emptyMap()
  val token: String?
    get() = null
}
