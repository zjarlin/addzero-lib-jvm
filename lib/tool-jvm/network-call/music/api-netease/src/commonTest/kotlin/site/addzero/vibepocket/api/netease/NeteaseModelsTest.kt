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
    val search = musicApi.searchSongs("稻香", 10, 0)
    println("Search Songs: ${search.toJsonByKtx()}")
  }

  @Test
  fun searchAlbums() = runTest {
    val search = musicApi.searchAlbums("依然范特西", 5, 0)
    println("Search Albums: ${search.toJsonByKtx()}")
  }

  @Test
  fun searchArtist() = runTest {
    val search = musicApi.searchArtist("周杰伦", 5, 0)
    println("Search Artist: ${search.toJsonByKtx()}")
  }

  @Test
  fun searchPlaylist() = runTest {
    val search = musicApi.searchPlaylist("周杰伦精选", 5, 0)
    println("Search Playlist: ${search.toJsonByKtx()}")
  }

  @Test
  fun searchLyric() = runTest {
    val search = musicApi.searchLyric("为你弹奏肖邦的夜曲", 5, 0)
    println("Search Lyric: ${search.toJsonByKtx()}")
  }

  @Test
  fun searchByKeyword() = runTest {
    val search = musicApi.searchByKeyword("四海尘烟又起")
    println("Search By Keyword (Union): ${search.toJsonByKtx()}")

    // 验证结果中既有普通匹配也有歌词匹配（如果有的话）
    search.result?.songs?.forEach { song ->
      if (!song.lyrics.isNullOrEmpty()) {
        println("Found song with lyrics: ${song.name} - ${song.matchedLyricText}")
      }
    }
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
