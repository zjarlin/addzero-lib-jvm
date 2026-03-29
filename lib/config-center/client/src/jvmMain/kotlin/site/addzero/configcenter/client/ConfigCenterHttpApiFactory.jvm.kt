package site.addzero.configcenter.client

import de.jensklingenberg.ktorfit.Ktorfit
import io.ktor.client.HttpClient

internal actual fun buildConfigCenterHttpApi(
    baseUrl: String,
    httpClient: HttpClient,
): ConfigCenterHttpApi {
    val ktorfit = Ktorfit.Builder()
        .baseUrl(baseUrl)
        .httpClient(httpClient)
        .build()
    return ktorfit.createConfigCenterHttpApi()
}
