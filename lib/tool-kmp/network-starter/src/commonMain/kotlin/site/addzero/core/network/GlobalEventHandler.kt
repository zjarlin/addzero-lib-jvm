package site.addzero.core.network

import io.ktor.client.statement.HttpResponse
import site.addzero.util.KoinInjector

interface HttpResponseEventHandlerSpi {
    val order: Int
        get() = 0

    fun handle(response: HttpResponse)
}

internal fun dispatchHttpResponseEvent(response: HttpResponse) {
    runCatching {
        KoinInjector.injectList<HttpResponseEventHandlerSpi>()
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
