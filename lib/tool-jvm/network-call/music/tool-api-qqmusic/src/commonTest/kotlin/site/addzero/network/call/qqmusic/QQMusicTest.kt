package site.addzero.network.call.qqmusic

import de.jensklingenberg.ktorfit.Ktorfit
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import kotlinx.coroutines.runBlocking
import site.addzero.core.network.apiClient
import site.addzero.network.call.qqmusic.model.*
import kotlin.test.*

/**
 * QQ 音乐 API 集成测试
 */
class QQMusicTest {

//    private val httpClient = HttpClient(CIO)

    private val mainApi = Ktorfit.Builder()
        .baseUrl("https://u.y.qq.com/")
        .httpClient(apiClient)
        .build()
        .createQQMusicMainApi()

    private val qzoneApi = Ktorfit.Builder()
        .baseUrl("https://i.y.qq.com/")
        .httpClient(apiClient)
        .build()
        .createQQMusicQzoneApi()

    private val qqMusic = QQMusic(mainApi, qzoneApi)

    @Test
    fun `搜索歌曲 - 周杰伦晴天`() = runBlocking {
        val result = qqMusic.search("周杰伦 晴天", SearchType.SONG, resultNum = 5)
        assertNotNull(result, "搜索结果不应为空")
        println("✓ 搜索歌曲成功: $result")
    }

    @Test
    fun `搜索专辑`() = runBlocking {
        val result = qqMusic.search("叶惠美", SearchType.ALBUM, resultNum = 3)
        assertNotNull(result, "搜索专辑结果不应为空")
        println("✓ 搜索专辑成功: $result")
    }

    @Test
    fun `搜索歌单`() = runBlocking {
        val result = qqMusic.search("华语经典", SearchType.PLAYLIST, resultNum = 3)
        assertNotNull(result, "搜索歌单结果不应为空")
        println("✓ 搜索歌单成功: $result")
    }


    @Test
    fun `获取歌曲播放URL`() = runBlocking {
        // 晴天的 songmid: 004Z8Ihr0JIu5s
        val url = qqMusic.getMusicUrl("004Z8Ihr0JIu5s", "320")
        println("✓ 获取音乐URL: $url")
    }

    @Test
    fun `获取歌词`() = runBlocking {
        val lyric = qqMusic.getLyric("004Z8Ihr0JIu5s")
        assertNotNull(lyric, "歌词不应为空")
        assertTrue(lyric.isNotBlank(), "歌词内容不应为空白")
        println("✓ 获取歌词成功，长度: ${lyric.length}")
        println(lyric.take(200))
    }

    @Test
    fun `获取解析后的歌词`() = runBlocking {
        val parsed = qqMusic.getParsedLyric("004Z8Ihr0JIu5s")
        assertTrue(parsed.count > 0, "歌词行数应大于0")
        println("✓ 解析歌词成功: ${parsed.count} 行")
        println("  标题: ${parsed.ti}")
        println("  歌手: ${parsed.ar}")
        parsed.lines.take(5).forEach { line ->
            println("  [${line.time}] ${line.lyric}")
        }
    }

    @Test
    fun `获取专辑歌曲列表`() = runBlocking {
        // 叶惠美专辑 albummid: 000MkMni19ClKG
        val songs = qqMusic.getAlbumSongList("000MkMni19ClKG")
        assertNotNull(songs, "专辑歌曲列表不应为空")
        assertTrue(songs.isNotEmpty(), "专辑应包含歌曲")
        println("✓ 获取专辑歌曲成功: ${songs.size} 首")
    }

    @Test
    fun `获取专辑名称`() = runBlocking {
        val name = qqMusic.getAlbumName("000MkMni19ClKG")
        assertNotNull(name, "专辑名称不应为空")
        println("✓ 获取专辑名称: $name")
    }

    @Test
    fun `获取歌手信息`() = runBlocking {
        // 周杰伦 singermid: 0025NhlN2yWrP4
        val info = qqMusic.getSingerInfo("0025NhlN2yWrP4")
        assertNotNull(info, "歌手信息不应为空")
        println("✓ 获取歌手信息成功: $info")
    }

    @Test
    fun `获取专辑封面图URL`() {
        val url = qqMusic.getAlbumCoverImage("000MkMni19ClKG")
        assertTrue(url.contains("000MkMni19ClKG"), "封面URL应包含专辑MID")
        assertTrue(url.startsWith("https://"), "封面URL应为HTTPS")
        println("✓ 专辑封面: $url")
    }
}

/**
 * 歌词解析器单元测试
 */
class LyricParserTest {

    @Test
    fun `解析带元信息的歌词`() {
        val response = LyricResponse(
            lyric = "[ti:晴天]\n[ar:周杰伦]\n[al:叶惠美]\n[by:]\n[offset:0]\n[00:00.00]晴天 - 周杰伦\n[00:04.50]故事的小黄花",
            trans = "[ti:晴天]\n[ar:周杰伦]\n[al:叶惠美]\n[by:]\n[offset:0]\n[00:00.00]Sunny Day\n[00:04.50]The little yellow flower of the story"
        )
        val parsed = LyricParser.parse(response)

        assertEquals("晴天", parsed.ti)
        assertEquals("周杰伦", parsed.ar)
        assertEquals("叶惠美", parsed.al)
        assertTrue(parsed.haveTrans)
        assertEquals(2, parsed.count)
        assertEquals("晴天 - 周杰伦", parsed.lines[0].lyric)
        assertEquals("Sunny Day", parsed.lines[0].trans)
        println("✓ 歌词解析测试通过")
    }

    @Test
    fun `解析无元信息的歌词`() {
        val response = LyricResponse(
            lyric = "[00:00.00]第一行歌词\n[00:05.00]第二行歌词",
            trans = ""
        )
        val parsed = LyricParser.parse(response)

        assertFalse(parsed.haveTrans)
        assertEquals(2, parsed.count)
        assertEquals("第一行歌词", parsed.lines[0].lyric)
        assertEquals("00:00.00", parsed.lines[0].time)
        println("✓ 无元信息歌词解析测试通过")
    }
}
