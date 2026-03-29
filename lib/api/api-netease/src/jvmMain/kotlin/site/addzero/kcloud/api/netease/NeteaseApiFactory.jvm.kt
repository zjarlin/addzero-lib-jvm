package site.addzero.kcloud.api.netease

import de.jensklingenberg.ktorfit.Ktorfit
import io.ktor.client.HttpClient

internal actual fun buildNeteaseApi(
    baseUrl: String,
    httpClient: HttpClient,
): NeteaseApi {
    return Ktorfit.Builder()
        .baseUrl(baseUrl)
        .httpClient(httpClient)
        .build()
        .createNeteaseApi()
}
