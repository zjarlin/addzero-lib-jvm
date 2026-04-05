package site.addzero.core.network

import io.ktor.client.statement.HttpResponse
import org.koin.mp.KoinPlatform
import site.addzero.core.network.spi.HttpResponseEventHandlerSpi


internal fun dispatchHttpResponseEvent(response: HttpResponse) {
  runCatching {
    KoinPlatform.getKoin().getAll<HttpResponseEventHandlerSpi>()
      .sortedBy(HttpResponseEventHandlerSpi::order)
  }.getOrDefault(emptyList()).forEach { handler ->
    runCatching {
      handler.handle(response)
    }.onFailure { error ->
      println(
        "HttpResponseEventHandlerSpi failed: ${handler::class.simpleName}: ${error.message}",
      )
    }
  }
}
