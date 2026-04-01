package site.addzero.kcloud.api.netease

import kotlinx.coroutines.test.runTest
import org.koin.mp.KoinPlatform
import kotlin.test.Test

/**
 * NeteaseApi 集成测试（需要网络）
 *
 * 覆盖 MusicSearchClient 的全部业务方法。
 */
@Suppress("NonAsciiCharacters")
class NeteaseModelsTest {
    private val client: MusicSearchClient
        get() = KoinPlatform.getKoin().get()

    @Test
    fun searchSongs() = runTest {
        val search = client.musicApi.search("稻香")
//        musicApi.
        println()
    }


}
