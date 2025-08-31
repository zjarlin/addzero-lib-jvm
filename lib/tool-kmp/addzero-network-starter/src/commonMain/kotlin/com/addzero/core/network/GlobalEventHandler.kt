package com.addzero.core.network

import io.ktor.client.statement.*

object GlobalEventDispatcher {
    lateinit var handler: ((HttpResponse) -> Unit)
}
