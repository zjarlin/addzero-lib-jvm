package site.addzero.network.call.musiclib.utils

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import kotlin.random.Random

/**
 * HTTP客户端管理
 */
object HttpClientManager {
    val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            val jsonConfig = Json {
                ignoreUnknownKeys = true
                isLenient = true
                coerceInputValues = true
            }
            json(jsonConfig)
            json(jsonConfig, contentType = ContentType.Text.Plain)
            json(jsonConfig, contentType = ContentType.Text.Html)
            json(jsonConfig, contentType = ContentType.parse("application/x-javascript"))
        }

        install(Logging) {
            level = LogLevel.NONE
        }

        install(HttpTimeout) {
            requestTimeoutMillis = 15000
            connectTimeoutMillis = 10000
        }

        defaultRequest {
            header(HttpHeaders.UserAgent, "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/121.0.0.0 Safari/537.36")
        }
    }

    fun close() {
        client.close()
    }
}

/**
 * 获取默认的HTTP请求配置
 */
fun HttpRequestBuilder.defaultHeaders(referer: String? = null, cookie: String? = null) {
    header(HttpHeaders.UserAgent, "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/121.0.0.0 Safari/537.36")
    referer?.let { header(HttpHeaders.Referrer, it) }
    cookie?.let { header(HttpHeaders.Cookie, it) }
    header("X-Forwarded-For", RandomChinaIP())
    header("X-Real-IP", RandomChinaIP())
}

/**
 * 获取移动端的HTTP请求配置
 */
fun HttpRequestBuilder.mobileHeaders(referer: String? = null, cookie: String? = null) {
    header(HttpHeaders.UserAgent, "Mozilla/5.0 (iPhone; CPU iPhone OS 13_2_3 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/13.0.3 Mobile/15E148 Safari/604.1")
    referer?.let { header(HttpHeaders.Referrer, it) }
    cookie?.let { header(HttpHeaders.Cookie, it) }
    header("X-Forwarded-For", RandomChinaIP())
    header("X-Real-IP", RandomChinaIP())
}
