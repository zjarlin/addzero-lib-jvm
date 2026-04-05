package site.addzero.core.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.darwin.Darwin

internal actual val httpClientEngineFactory: HttpClientEngineFactory<*> = Darwin
