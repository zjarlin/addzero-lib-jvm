package site.addzero.network.call.music

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.TestInstance.Lifecycle
import site.addzero.network.call.musiclib.api.netease.NeteaseAPI

/**
 * 网易云音乐 API 测试
 * 注意: 这些测试需要网络连接
 */
@TestInstance(Lifecycle.PER_CLASS)
@Disabled
class NeteaseAPITest {

  private lateinit var api: NeteaseAPI

  @BeforeAll
  fun setup() {
    api = NeteaseAPI()
  }

  @Test
  @DisplayName("测试搜索歌曲")
  fun testSearch() = runBlocking {
    val results = api.search("周杰伦")
    assertTrue(results.isNotEmpty(), "搜索结果不应为空")
    val firstSong = results.first()
    assertNotNull(firstSong.id)
    assertNotNull(firstSong.name)
    assertNotNull(firstSong.artist)
    assertEquals("netease", firstSong.source)

    println("搜索到 ${results.size} 首歌曲")
    println("第一首: ${firstSong.name} - ${firstSong.artist}")
  }

  @Test
  @DisplayName("测试搜索歌单")
  fun testSearchPlaylist() = runBlocking {
    val results = api.searchPlaylist("流行")

    assertTrue(results.isNotEmpty(), "搜索歌单结果不应为空")

    val firstPlaylist = results.first()
    assertNotNull(firstPlaylist.id)
    assertNotNull(firstPlaylist.name)
    assertEquals("netease", firstPlaylist.source)

    println("搜索到 ${results.size} 个歌单")
    println("第一个: ${firstPlaylist.name} (${firstPlaylist.trackCount} 首)")
  }

  @Test
  @DisplayName("测试获取推荐歌单")
  fun testGetRecommendedPlaylists() = runBlocking {
    val results = api.getRecommendedPlaylists()

    assertTrue(results.isNotEmpty(), "推荐歌单不应为空")

    val firstPlaylist = results.first()
    assertNotNull(firstPlaylist.id)
    assertNotNull(firstPlaylist.name)

    println("获取到 ${results.size} 个推荐歌单")
    println("第一个: ${firstPlaylist.name}")
  }

  @Test
  @DisplayName("测试解析歌曲链接")
  fun testParse() = runBlocking {
    // 使用已知的网易云歌曲链接
    val link = "https://music.163.com/#/song?id=186016"

    val song = api.parse(link)

    assertNotNull(song)
    assertEquals("186016", song.id)
    assertEquals("netease", song.source)

    println("解析歌曲: ${song.name} - ${song.artist}")
  }

  @Test
  @DisplayName("测试解析无效链接应抛出异常")
  fun testParseInvalidLink(): Unit = runBlocking {
    val invalidLink = "https://example.com/invalid"

    assertThrows<IllegalArgumentException> {
      runBlocking { api.parse(invalidLink) }
    }
  }

  @Test
  @DisplayName("测试非 VIP 账号检测")
  fun testIsVipAccountWithoutCookie() = runBlocking {
    val isVip = api.isVipAccount()

    assertFalse(isVip, "无 Cookie 时应返回非 VIP")
  }

  @Test
  @DisplayName("测试获取歌曲下载链接")
//    @Disabled("此测试需要有效的歌曲对象，且可能触发风控")
  fun testGetDownloadURL() = runBlocking {
    val searchResults = api.search("阿刁")
    assumeTrue(searchResults.isNotEmpty(), "需要搜索结果才能测试")

    val song = searchResults.first()
    val url = api.getDownloadURL(song)

    assertTrue(url.isNotEmpty())
    assertTrue(url.startsWith("http"))
  }

  @Test
  @DisplayName("测试获取歌词")
  @Disabled("此测试需要有效的歌曲对象")
  fun testGetLyrics() = runBlocking {
    val searchResults = api.search("晴天")
    assumeTrue(searchResults.isNotEmpty(), "需要搜索结果才能测试")

    val song = searchResults.first()
    val lyrics = api.getLyrics(song)

    assertTrue(lyrics.isNotEmpty())
    println("歌词前100字符: ${lyrics.take(100)}")
  }
}
