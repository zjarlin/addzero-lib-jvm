package site.addzero.events

import site.addzero.core.network.GlobalEventDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.annotation.Single

@Single(createdAtStart = true)
fun emitEventBus() {
    GlobalEventDispatcher.handler = {
        CoroutineScope(Dispatchers.Main).launch {
            EventBus.emit(it)
        }
    }
}
