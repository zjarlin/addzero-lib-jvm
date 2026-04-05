package site.addzero.kcloud.api.netease

import org.koin.core.annotation.Factory
import org.koin.core.annotation.Module
import site.addzero.core.network.HttpClientFactory

@Module
class NeteaseApiBindings {
    @Factory
    fun provideNeteaseApi(
        httpClientFactory: HttpClientFactory,
    ): NeteaseApi {
        return buildNeteaseApi(
            baseUrl = MusicSearchClient.API_BASE_URL,
            httpClient = httpClientFactory.get(MusicSearchClient.CLIENT_PROFILE),
        )
    }
}
