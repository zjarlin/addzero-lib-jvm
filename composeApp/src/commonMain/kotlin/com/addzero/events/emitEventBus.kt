package com.addzero.events

import com.addzero.core.network.GlobalEventDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.annotation.Single

@Single(createdAtStart = true)
fun emitEventBus() {
    println("监听事件总线开启")
    GlobalEventDispatcher.handler = {
        CoroutineScope(Dispatchers.Main).launch {
            EventBus.emit(it)
        }
    }
}
