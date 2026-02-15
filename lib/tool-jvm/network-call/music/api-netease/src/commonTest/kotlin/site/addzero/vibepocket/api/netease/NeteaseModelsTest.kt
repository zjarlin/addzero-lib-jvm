package site.addzero.vibepocket.api.netease

import kotlinx.coroutines.test.runTest
import site.addzero.core.ext.toJsonByKtx
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

  @Test
  fun `获取歌曲详情`() {
    runTest {
//      val search = musicApi.search("稻香")
      val id = "2651425710"

      val songIds = listOf(186016L, 186017L)
      val idsParam = songIds.joinToString(separator = ",", prefix = "[", postfix = "]")

      val songDetail = musicApi.getSongDetail(idsParam)
//      songDetail.toJsonByKtx()

      println("Song Detail Response: $songDetail")
    }

  }
  @Test
  fun `获取歌词`() {
    val lng = 186016L
    runTest {

      val lyric = musicApi.getLyric(lng)
      val toJsonByKtx = lyric.toJsonByKtx()
      println(toJsonByKtx)
    }
  }

}
