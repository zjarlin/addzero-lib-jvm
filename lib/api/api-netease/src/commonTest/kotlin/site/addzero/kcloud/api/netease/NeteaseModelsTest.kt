package site.addzero.kcloud.api.netease

import kotlinx.coroutines.test.runTest
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.mp.KoinPlatform
import site.addzero.core.network.HttpClientFactory
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

/**
 * NeteaseApi 集成测试（需要网络）
 *
 * 覆盖 MusicSearchClient 的全部业务方法。
 */
@Suppress("NonAsciiCharacters")
class NeteaseModelsTest {
    @BeforeTest
    fun setUp() {
        stopKoin()
        startKoin {
            modules(
                module {
                    single { HttpClientFactory() }
                    single { MusicSearchClient(get()) }
                },
            )
        }
    }

    @AfterTest
    fun tearDown() {
        stopKoin()
    }

    private val client: MusicSearchClient
        get() = KoinPlatform.getKoin().get()

    @Test
    fun searchSongs() = runTest {
        val search = client.musicApi.search("稻香")
//        musicApi.
        println()
    }


}
