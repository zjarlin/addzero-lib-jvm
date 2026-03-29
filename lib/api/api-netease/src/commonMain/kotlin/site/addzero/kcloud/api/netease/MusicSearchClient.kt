package site.addzero.kcloud.api.netease

import io.ktor.client.HttpClient
import org.koin.core.annotation.Single
import site.addzero.core.network.ApiClients

internal expect fun buildNeteaseApi(
    baseUrl: String,
    httpClient: HttpClient,
): NeteaseApi

/**
 * 网易云音乐 API 客户端。
 *
 * - endpoint 由 network-starter 的 `ApiClientSpi` 提供
 * - token / headers / SSE / WebSocket 开关统一走 `ApiClients`
 */
@Single
class MusicSearchClient(
    private val apiClients: ApiClients,
) {
    companion object {
        const val CLIENT_PROFILE = "netease-music"
        const val API_ENDPOINT = "netease-music-api"

        fun shared(): MusicSearchClient {
            return MusicSearchClient(ApiClients.shared())
        }
    }

    var mytoken: String? = null
        set(value) {
            field = value
            apiClients.setBearerToken(API_ENDPOINT, value)
        }

    val musicApi: NeteaseApi
        get() = apiClients.create(API_ENDPOINT, ::buildNeteaseApi)

    init {
        mytoken = null
    }
}
