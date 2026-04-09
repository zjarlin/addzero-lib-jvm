package site.addzero.core.network

import de.jensklingenberg.ktorfit.Ktorfit
import io.ktor.client.*
import io.ktor.client.engine.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.api.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.plugins.sse.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.koin.mp.KoinPlatform
import site.addzero.core.network.json.json
import site.addzero.core.network.spi.HttpClientProfileSpi
import site.addzero.core.network.token.TokenManager
import site.addzero.ktor2curl.CurlLogger
import site.addzero.ktor2curl.KtorToCurl
import kotlin.time.Duration.Companion.minutes

internal expect val httpClientEngineFactory: HttpClientEngineFactory<*>

/**
 * curl日志
 */
private fun HttpClientConfig<*>.configCurl() {

  install(KtorToCurl) {
    converter = object : CurlLogger {
      override fun log(curl: String) {
        println(curl)
      }
    }
  }
}

/**
 * 日志
 */
private fun HttpClientConfig<*>.configLog() {
  install(Logging) {
    logger = Logger.DEFAULT
    level = LogLevel.ALL
  }
}

/**
 * json配置
 */
private fun HttpClientConfig<*>.configJson() {
  install(ContentNegotiation) {
    json(json)
  }
}

/**
 * 把非 2xx HTTP 响应转换成更可读的错误文本。
 */
private fun HttpClientConfig<*>.configHttpErrors() {
  HttpResponseValidator {
    validateResponse { response ->
      if (response.status.isSuccess()) {
        return@validateResponse
      }
      val rawBody = runCatching { response.bodyAsText() }.getOrNull()
      val reason = rawBody.extractHttpErrorMessage()
      throw IllegalStateException(reason ?: "HTTP ${response.status.value} ${response.status.description}")
    }
  }
}


/**
 * 配置超时时间 - 针对ai接口的长时间响应
 */
private fun HttpClientConfig<*>.configtimeout() {
  install(HttpTimeout) {
    // 请求超时时间 - 5分钟，适合ai接口的长时间处理
    requestTimeoutMillis = 5.minutes.inWholeSeconds
    // 连接超时时间 - 30秒
    connectTimeoutMillis = 30_000
    // socket超时时间 - 5分钟
    socketTimeoutMillis = 5.minutes.inWholeMilliseconds

  }
}

private fun HttpClientConfig<*>.configHeadersWithStream() {
  defaultRequest {
    // 添加基础请求头
    headers {
      append(HttpHeaders.Accept, "text/event-stream")
      append(HttpHeaders.ContentType, "application/json")
    }
  }
}


private fun HttpClientConfig<*>.configHeadersWithJson() {
  defaultRequest {
    // 添加基础请求头
    headers {
      append(HttpHeaders.Accept, "application/json")
      append(HttpHeaders.ContentType, "application/json")
    }
  }
}


/**
 * 响应拦截器
 */
private fun HttpClientConfig<*>.configResPonse() {
  install(createClientPlugin("HttpResponseInterceptor") {
    onResponse { response ->
      val error = response.status.value != HttpStatusCode.OK.value
      if (error) {
        val orNull = runCatching {
          // 在协程作用域内执行挂起操作
          response.bodyAsText()
        }.getOrNull()
        println("异常body: $orNull")
//          GlobalEventDispatcher.handler(response)
      }
    }
  })

}

private fun HttpClientConfig<*>.configToken(token: String?) {
  defaultRequest {
    headers {
      token ?: KoinPlatform.getKoin().get<TokenManager>().getToken()?.let {
        append(HttpHeaders.Authorization, it)
      }
    }
  }
}

fun HttpClient.setToken(token: String?): HttpClient {
  return this.config {
    configToken(token)
  }
}

fun HttpClient.enableSSE(): HttpClient {
  return this.config {
    install(SSE) {
      showCommentEvents()
      showRetryEvents()
    }
  }
}

fun HttpClientProfileSpi.toHttpClient(): HttpClient {
  val spi = this
  val httpClient = HttpClient(httpClientEngineFactory)
  val config = httpClient.config {
    defaultRequest {
      url(spi.baseUrl)
    }
    configCurl()
    configHeadersWithJson()
    configLog()
    configJson()
    configHttpErrors()
    install(KtorToCurl) {
      converter = object : CurlLogger {
        override fun log(curl: String) {
          if (spi.enableCurlLogging) {
            println(curl)
          }
        }
      }
    }
    configToken(token)
  }
  return config
}

val apiClient: HttpClient
  get() = KoinPlatform.getKoin().get()

val ktorfit: Ktorfit
  get() = KoinPlatform.getKoin().get()

/**
 * 从服务端错误响应正文中提取更直观的消息。
 */
private fun String?.extractHttpErrorMessage(): String? {
  val text = this?.trim().orEmpty()
  if (text.isBlank()) {
    return null
  }
  return runCatching {
    val payload = json.parseToJsonElement(text).jsonObject
    payload["message"]?.jsonPrimitive?.contentOrNull
      ?: payload["msg"]?.jsonPrimitive?.contentOrNull
      ?: payload["error"]?.jsonPrimitive?.contentOrNull
      ?: text
  }.getOrElse {
    text
  }
}

