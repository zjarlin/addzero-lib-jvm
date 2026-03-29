package site.addzero.kcloud.api.netease

import org.koin.core.annotation.Factory
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single
import site.addzero.core.network.ApiClientSpi

@Single
class NeteaseApiClient : ApiClientSpi {
    override val endpointId: String = MusicSearchClient.API_ENDPOINT

    override val baseUrl: String = "https://music.163.com/api/"

    override val clientProfile: String = MusicSearchClient.CLIENT_PROFILE
}

@Module
class NeteaseApiBindings {
    @Factory
    fun provideNeteaseApi(
        client: MusicSearchClient,
    ): NeteaseApi {
        return client.musicApi
    }
}
