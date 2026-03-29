package site.addzero.core.network

import io.ktor.client.engine.*
import io.ktor.client.engine.js.*

internal actual val addZeroHttpClientEngineFactory: HttpClientEngineFactory<*> = Js
