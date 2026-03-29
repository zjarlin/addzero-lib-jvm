package site.addzero.core.network

import io.ktor.client.engine.*
import io.ktor.client.engine.darwin.*

internal actual val addZeroHttpClientEngineFactory: HttpClientEngineFactory<*> = Darwin
