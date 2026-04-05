package site.addzero.kcloud.music.suno

import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Module
import org.koin.core.annotation.Property
import org.koin.core.annotation.Single
import site.addzero.core.network.spi.HttpClientProfileSpi
import site.addzero.kcloud.api.suno.SunoApiClient

@Single
class SunoHttpClientProfileSpi : HttpClientProfileSpi {
    override val profile: String = SunoApiClient.HTTP_CLIENT_PROFILE
    override val headers: Map<String, String> = mapOf(
        HttpHeaders.Accept to ContentType.Application.Json.toString(),
    )
}

@Module
class SunoApiBindings {
    @Factory
    fun provideSunoApiClient(
        @Property("suno.apiToken") token: String,
    ): SunoApiClient {
        return SunoApiClient(apiToken = token)
    }
}
