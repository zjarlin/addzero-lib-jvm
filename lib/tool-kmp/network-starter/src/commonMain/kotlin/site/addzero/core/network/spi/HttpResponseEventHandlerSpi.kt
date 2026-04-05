package site.addzero.core.network.spi

import io.ktor.client.statement.HttpResponse
import site.addzero.tool.coll.TopologicalSpi

interface HttpResponseEventHandlerSpi : TopologicalSpi {
  fun handle(response: HttpResponse)
}
