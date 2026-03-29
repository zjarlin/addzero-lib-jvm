package site.addzero.kcloud.api.netease

import kotlinx.coroutines.test.runTest
import kotlin.test.Test

/**
 * NeteaseApi 集成测试（需要网络）
 *
 * 覆盖 MusicSearchClient 的全部业务方法。
 */
@Suppress("NonAsciiCharacters")
class NeteaseModelsTest {

    private val client = MusicSearchClient.shared()

    @Test
    fun searchSongs() = runTest {
        val search = client.musicApi.search("稻香")
//        musicApi.
        println()
    }


}
