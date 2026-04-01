package site.addzero.core.network

import io.ktor.client.HttpClient
import org.koin.core.annotation.Single


interface ApiClientSpi {
  val endpointId get() = this::class.simpleName!!
  val baseUrl: String
  val enableSse: Boolean
    get() = false
  val enableWebSocket: Boolean
    get() = false
  val headers: Map<String, String>
    get() = emptyMap()
  val bearerToken: String?
    get() = null
}

@Single
class ApiClients(
  private val apiClientSpis: List<ApiClientSpi>,
  private val httpClientFactory: HttpClientFactory,
) {

  fun setBearerToken(endpointId: String, token: String?) {
    val spi = spi(endpointId)
    httpClientFactory.setBearerToken(spi.endpointId, token)
  }

  fun <T> create(
    endpointId: String,
    builder: (baseUrl: String, httpClient: HttpClient) -> T,
  ): T {
    val spi = spi(endpointId)
    configureProfile(spi)
    return builder(spi.baseUrl, httpClientFactory.get(spi.endpointId))
  }

  private fun configureProfile(spi: ApiClientSpi) {
    httpClientFactory.clearHeaders(spi.endpointId)
    spi.headers.forEach { (name, value) ->
      httpClientFactory.putHeader(spi.endpointId, name, value)
    }
    httpClientFactory.setBearerToken(spi.endpointId, spi.bearerToken)
    httpClientFactory.setSseEnabled(spi.endpointId, spi.enableSse)
    httpClientFactory.setWebSocketEnabled(spi.endpointId, spi.enableWebSocket)
  }

  private fun spi(endpointId: String): ApiClientSpi {
    return apiClientSpis.firstOrNull { it.endpointId == endpointId }
      ?: error("No ApiClientSpi registered for endpointId=$endpointId")
  }
}


