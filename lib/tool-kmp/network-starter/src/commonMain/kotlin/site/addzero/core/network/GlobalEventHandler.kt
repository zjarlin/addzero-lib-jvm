package site.addzero.core.network

import io.ktor.client.statement.HttpResponse
import org.koin.mp.KoinPlatform

/**
 * 拓扑Spi接口
 * @author zjarlin
 * @date 2026/03/29
 * @constructor 创建[TopologicalSpi]
 */
interface TopologicalSpi {
  val key: String
    get() = this::class.simpleName!!
  val order: Int
    get() = Int.MAX_VALUE
  val dependsOn: String?
    get() = null
}

interface HttpResponseEventHandlerSpi : TopologicalSpi {
  fun handle(response: HttpResponse)
}

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
