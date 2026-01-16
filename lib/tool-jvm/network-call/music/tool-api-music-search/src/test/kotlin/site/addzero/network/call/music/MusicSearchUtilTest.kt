package site.addzero.network.call.music

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Tag

/**
 * 音乐搜索工具类测试
 */
@DisplayName("音乐搜索工具类测试")
class MusicSearchUtilTest {

    @Test
    @DisplayName("测试工具类搜索歌曲")
    @Tag("integration")
    fun testSearchSongs() {
        // When
        val songs = MusicSearchUtil.searchSongs("晴天", limit = 3)

        // Then
        assertNotNull(songs)
        assertTrue(songs.isNotEmpty())

        println("✓ 工具类搜索成功: ${songs.size} 首歌曲")
    }

    @Test
    @DisplayName("测试工具类精确搜索")
    @Tag("integration")
    fun testSearchBySongAndArtist() {
        // When
        val songs = MusicSearchUtil.searchBySongAndArtist("晴天", "周杰伦")

        // Then
        assertNotNull(songs)
        assertTrue(songs.isNotEmpty())

        println("✓ 工具类精确搜索成功: ${songs.size} 首歌曲")
    }

    @Test
    @DisplayName("测试工具类搜索歌手")
    @Tag("integration")
    fun testSearchArtists() {
        // When
        val artists = MusicSearchUtil.searchArtists("周杰伦", limit = 2)

        // Then
        assertNotNull(artists)
        assertTrue(artists.isNotEmpty())

        println("✓ 工具类搜索歌手成功: ${artists.size} 位")
    }

    @Test
    @DisplayName("测试工具类搜索专辑")
    @Tag("integration")
    fun testSearchAlbums() {
        // When
        val albums = MusicSearchUtil.searchAlbums("叶惠美", limit = 2)

        // Then
        assertNotNull(albums)
        assertTrue(albums.isNotEmpty())

        println("✓ 工具类搜索专辑成功: ${albums.size} 张")
    }

    @Test
    @DisplayName("测试工具类搜索歌单")
    @Tag("integration")
    fun testSearchPlaylists() {
        // When
        val playlists = MusicSearchUtil.searchPlaylists("华语经典", limit = 2)

        // Then
        assertNotNull(playlists)
        assertTrue(playlists.isNotEmpty())

        println("✓ 工具类搜索歌单成功: ${playlists.size} 个")
    }

    @Test
    @DisplayName("测试工具类获取歌词")
    @Tag("integration")
    fun testGetLyric() {
        // When
        val lyric = MusicSearchUtil.getLyric(186016L)

        // Then
        assertNotNull(lyric)
        assertNotNull(lyric.lrc?.lyric)

        println("✓ 工具类获取歌词成功")
    }

    @Test
    @DisplayName("测试工具类根据歌词搜索")
    @Tag("integration")
    fun testSearchByLyric() {
        // When
        val songs = MusicSearchUtil.searchByLyric("刮风这天我试过握着你手")

        // Then
        assertNotNull(songs)
        assertTrue(songs.isNotEmpty())

        println("✓ 工具类根据歌词搜索成功: ${songs.size} 首")
    }

    @Test
    @DisplayName("测试工具类获取歌曲详情")
    @Tag("integration")
    fun testGetSongDetail() {
        // When
        val song = MusicSearchUtil.getSongDetail(186016L)

        // Then
        assertNotNull(song)
        assertEquals(186016L, song?.id)

        println("✓ 工具类获取歌曲详情成功: ${song?.name}")
    }

    @Test
    @DisplayName("测试工具类批量获取歌曲详情")
    @Tag("integration")
    fun testGetSongDetails() {
        // When
        val songs = MusicSearchUtil.getSongDetails(listOf(186016L, 186017L))

        // Then
        assertNotNull(songs)
        assertEquals(2, songs.size)

        println("✓ 工具类批量获取详情成功: ${songs.size} 首")
    }

    @Test
    @DisplayName("测试工具类搜索并获取歌词")
    @Tag("integration")
    fun testSearchWithLyrics() {
        // When
        val songsWithLyrics = MusicSearchUtil.searchWithLyrics("晴天", "周杰伦")

        // Then
        assertNotNull(songsWithLyrics)
        assertTrue(songsWithLyrics.isNotEmpty())

        val result = songsWithLyrics.first()
        assertNotNull(result.song)

        println("✓ 搜索并获取歌词成功")
        println("  歌曲: ${result.song.name}")
        println("  歌词: ${if (result.lyric != null) "已获取" else "未获取"}")
    }

       @Test
    @DisplayName("测试工具类根据歌名获取歌词")
    @Tag("integration")
    fun testGetLyricBySongName() {
        // When
        val lyric = MusicSearchUtil.getLyric("江湖夜雨十年灯", "空想之喵")

        // Then
        assertNotNull(lyric)
        assertNotNull(lyric?.lrc?.lyric)

        println("✓ 工具类根据歌名获取歌词成功")
        println("  歌词预览: ${lyric?.lrc?.lyric?.take(100)}...")
    }

    @Test
    @DisplayName("测试工具类根据歌词片段获取完整歌词")
    @Tag("integration")
    fun testGetLyricsByFragment() {
        // When
        val results = MusicSearchUtil.getLyricsByFragment("刮风这天我试过握着你手", limit = 3)

        // Then
        assertNotNull(results)
        assertTrue(results.isNotEmpty())

        results.forEach { result ->
            assertNotNull(result.song)
            assertNotNull(result.lyric)
            assertNotNull(result.lyric.lrc?.lyric)
        }

        println("✓ 工具类根据歌词片段获取完整歌词成功")
        println("  找到 ${results.size} 首歌曲")
        results.forEach { result ->
            println("    - ${result.song.name} - ${result.song.artists.joinToString { it.name }}")
        }
    }

}
