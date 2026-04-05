package site.addzero.core.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.js.Js

internal actual val httpClientEngineFactory: HttpClientEngineFactory<*> = Js
