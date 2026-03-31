package site.addzero.kcloud.api.netease

import org.koin.core.annotation.Factory
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single
import site.addzero.core.network.ApiClientSpi

@Single
class NeteaseApiClient : ApiClientSpi {
    override val baseUrl = "https://music.163.com/api/"
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
