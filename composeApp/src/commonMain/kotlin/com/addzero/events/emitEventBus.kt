package com.addzero.events

import com.addzero.core.network.GlobalEventDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

fun emitEventBus() {
    GlobalEventDispatcher.handler = {
        CoroutineScope(Dispatchers.Main).launch {
            EventBus.emit(it)
        }
    }
}
