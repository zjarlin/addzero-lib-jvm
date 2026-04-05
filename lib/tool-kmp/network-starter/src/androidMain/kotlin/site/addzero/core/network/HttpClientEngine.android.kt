package site.addzero.core.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO

internal actual val httpClientEngineFactory: HttpClientEngineFactory<*> = CIO
