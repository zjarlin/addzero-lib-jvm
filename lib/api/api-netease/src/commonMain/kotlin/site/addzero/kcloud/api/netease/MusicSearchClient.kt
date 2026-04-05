package site.addzero.kcloud.api.netease

import io.ktor.client.HttpClient
import org.koin.core.annotation.Single
import site.addzero.core.network.HttpClientFactory

internal expect fun buildNeteaseApi(
    baseUrl: String,
    httpClient: HttpClient,
): NeteaseApi

/**
 * 网易云音乐 API 客户端。
 */
@Single
class MusicSearchClient(
    private val httpClientFactory: HttpClientFactory,
) {
    companion object {
        const val CLIENT_PROFILE = "netease-music"
        const val API_BASE_URL = "https://music.163.com/api/"
    }

    var mytoken: String? = null
        set(value) {
            field = value
            httpClientFactory.setBearerToken(CLIENT_PROFILE, value)
        }

    val musicApi: NeteaseApi
        get() = buildNeteaseApi(
            baseUrl = API_BASE_URL,
            httpClient = httpClientFactory.get(CLIENT_PROFILE),
        )

    init {
        mytoken = null
    }
}
