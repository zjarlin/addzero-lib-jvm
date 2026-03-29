package site.addzero.kcloud.api.suno

import de.jensklingenberg.ktorfit.Ktorfit
import io.ktor.client.HttpClient

internal actual fun buildSunoApi(
    baseUrl: String,
    httpClient: HttpClient,
): SunoApi {
    return Ktorfit.Builder()
        .baseUrl(baseUrl)
        .httpClient(httpClient)
        .build()
        .createSunoApi()
}
