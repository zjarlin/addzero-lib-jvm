package site.addzero.vibepocket.api.netease

import kotlinx.coroutines.test.runTest
import site.addzero.vibepocket.api.netease.MusicSearchClient.musicApi
import kotlin.test.Test

/**
 * NeteaseApi 集成测试（需要网络）
 *
 * 覆盖 MusicSearchClient 的全部业务方法。
 */
@Suppress("NonAsciiCharacters")
class NeteaseModelsTest {

    private val client = MusicSearchClient

    @Test
    fun searchSongs() = runTest {
        val search = musicApi.search("稻香")
//        musicApi.
        println()
    }


}
