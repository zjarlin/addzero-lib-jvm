package site.addzero.core.network

import io.ktor.client.engine.*
import io.ktor.client.engine.cio.*

internal actual val addZeroHttpClientEngineFactory: HttpClientEngineFactory<*> = CIO
