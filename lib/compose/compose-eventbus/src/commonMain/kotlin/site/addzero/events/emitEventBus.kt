package site.addzero.events

import io.ktor.client.statement.HttpResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.annotation.Single
import site.addzero.core.network.spi.HttpResponseEventHandlerSpi

@Single(createdAtStart = true)
class EventBusHttpResponseHandler : HttpResponseEventHandlerSpi {
    override fun handle(response: HttpResponse) {
        CoroutineScope(Dispatchers.Main).launch {
            EventBus.emit(response)
        }
    }
}
