package site.addzero.core.network

import io.ktor.client.HttpClient
import org.koin.core.annotation.Single
import site.addzero.util.KoinInjector


interface ApiClientSpi {
  val endpointId: String
  val baseUrl: String
  val clientProfile: String
    get() = endpointId
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
  companion object {
    fun shared(): ApiClients {
      return KoinInjector.inject()
    }
  }

  fun setBearerToken(endpointId: String, token: String?) {
    val spi = spi(endpointId)
    httpClientFactory.setBearerToken(spi.clientProfile, token)
  }

  fun <T> create(
    endpointId: String,
    builder: (baseUrl: String, httpClient: HttpClient) -> T,
  ): T {
    val spi = spi(endpointId)
    configureProfile(spi)
    return builder(spi.baseUrl, httpClientFactory.get(spi.clientProfile))
  }

  private fun configureProfile(spi: ApiClientSpi) {
    httpClientFactory.clearHeaders(spi.clientProfile)
    spi.headers.forEach { (name, value) ->
      httpClientFactory.putHeader(spi.clientProfile, name, value)
    }
    httpClientFactory.setBearerToken(spi.clientProfile, spi.bearerToken)
    httpClientFactory.setSseEnabled(spi.clientProfile, spi.enableSse)
    httpClientFactory.setWebSocketEnabled(spi.clientProfile, spi.enableWebSocket)
  }

  private fun spi(endpointId: String): ApiClientSpi {
    return apiClientSpis.firstOrNull { it.endpointId == endpointId }
      ?: error("No ApiClientSpi registered for endpointId=$endpointId")
  }
}



