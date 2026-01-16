package site.addzero.network.call.music

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Tag

/**
 * 音乐搜索客户端测试
 */
@DisplayName("音乐搜索客户端测试")
class MusicSearchClientTest {

    private val client = MusicSearchClient()

    @Test
    @DisplayName("测试搜索歌曲功能")
    @Tag("integration")
    fun testSearchSongs() {
        // Given
        val keywords = "晴天"
        val limit = 5

        // When
        val songs = client.searchSongs(keywords, limit)

        // Then
        assertNotNull(songs)
        assertTrue(songs.isNotEmpty())
        assertTrue(songs.size <= limit)

        val firstSong = songs.first()
        assertNotNull(firstSong.id)
        assertNotNull(firstSong.name)
        assertTrue(firstSong.artists.isNotEmpty())
        assertNotNull(firstSong.album)

        println("✓ 搜索到 ${songs.size} 首歌曲")
        println("  第一首: ${firstSong.name} - ${firstSong.artists.joinToString { it.name }}")
    }

    @Test
    @DisplayName("测试根据歌名和歌手搜索")
    @Tag("integration")
    fun testSearchBySongAndArtist() {
        // Given
        val songName = "你要的全拿走"
        val artistName = "胡彦斌"

        // When
        val songs = client.searchBySongAndArtist(songName, artistName)

        // Then
        assertNotNull(songs)
        assertTrue(songs.isNotEmpty())

        songs.forEach { song ->
            val hasArtist = song.artists.any { it.name.contains(artistName, ignoreCase = true) }
            assertTrue(hasArtist)
        }

        println("✓ 找到 ${songs.size} 首匹配的歌曲")
    }

    @Test
    @DisplayName("测试搜索歌手功能")
    @Tag("integration")
    fun testSearchArtists() {
        // Given
        val keywords = "周杰伦"
        val limit = 3

        // When
        val artists = client.searchArtists(keywords, limit)

        // Then
        assertNotNull(artists)
        assertTrue(artists.isNotEmpty())

        val firstArtist = artists.first()
        assertNotNull(firstArtist.id)
        assertNotNull(firstArtist.name)

        println("✓ 搜索到 ${artists.size} 位歌手")
        println("  第一位: ${firstArtist.name}")
    }

    @Test
    @DisplayName("测试搜索专辑功能")
    @Tag("integration")
    fun testSearchAlbums() {
        // Given
        val keywords = "叶惠美"
        val limit = 3

        // When
        val albums = client.searchAlbums(keywords, limit)

        // Then
        assertNotNull(albums)
        assertTrue(albums.isNotEmpty())

        val firstAlbum = albums.first()
        assertNotNull(firstAlbum.id)
        assertNotNull(firstAlbum.name)

        println("✓ 搜索到 ${albums.size} 张专辑")
    }

    @Test
    @DisplayName("测试搜索歌单功能")
    @Tag("integration")
    fun testSearchPlaylists() {
        // Given
        val keywords = "华语经典"
        val limit = 3

        // When
        val playlists = client.searchPlaylists(keywords, limit)

        // Then
        assertNotNull(playlists)
        assertTrue(playlists.isNotEmpty())

        val firstPlaylist = playlists.first()
        assertNotNull(firstPlaylist.id)
        assertNotNull(firstPlaylist.name)

        println("✓ 搜索到 ${playlists.size} 个歌单")
    }

    @Test
    @DisplayName("测试获取歌词功能")
    @Tag("integration")
    fun testGetLyric() {
        // Given
        val songId = 186016L

        // When
        val lyricResponse = client.getLyric(songId)

        // Then
        assertNotNull(lyricResponse)
        assertEquals(200, lyricResponse.code)
        assertNotNull(lyricResponse.lrc)
        assertNotNull(lyricResponse.lrc?.lyric)
        assertTrue(lyricResponse.lrc?.lyric?.isNotEmpty() == true)

        println("✓ 成功获取歌词")
    }

    @Test
    @DisplayName("测试根据歌词搜索功能")
    @Tag("integration")
    fun testSearchByLyric() {
        // Given
        val lyricFragment = "刮风这天我试过握着你手"

        // When
        val songs = client.searchByLyric(lyricFragment)

        // Then
        assertNotNull(songs)
        assertTrue(songs.isNotEmpty())

        println("✓ 根据歌词找到 ${songs.size} 首歌曲")
    }

    @Test
    @DisplayName("测试获取歌曲详情功能")
    @Tag("integration")
    fun testGetSongDetail() {
        // Given
        val songIds = listOf(186016L, 186017L)

        // When
        val songs = client.getSongDetail(songIds)

        // Then
        assertNotNull(songs)
        assertEquals(songIds.size, songs.size)

        songs.forEach { song ->
            assertNotNull(song.id)
            assertNotNull(song.name)
            assertTrue(songIds.contains(song.id))
        }

        println("✓ 成功获取 ${songs.size} 首歌曲详情")
    }

    @Test
    @DisplayName("测试搜索结果分页")
    @Tag("integration")
    fun testSearchPagination() {
        // Given
        val keywords = "周杰伦"
        val limit = 5

        // When
        val firstPage = client.searchSongs(keywords, limit, 0)
        val secondPage = client.searchSongs(keywords, limit, limit)

        // Then
        assertNotNull(firstPage)
        assertNotNull(secondPage)

        val firstPageIds = firstPage.map { it.id }.toSet()
        val secondPageIds = secondPage.map { it.id }.toSet()
        val intersection = firstPageIds.intersect(secondPageIds)

        assertTrue(intersection.isEmpty())

        println("✓ 分页功能正常")
    }

    @Test
    @DisplayName("测试根据歌名获取歌词")
    @Tag("integration")
    fun testGetLyricBySongName() {
        // Given
        val songName = "晴天"
        val artistName = "周杰伦"

        // When
        val lyricResponse = client.getLyricBySongName(songName, artistName)

        // Then
        assertNotNull(lyricResponse)
        assertEquals(200, lyricResponse?.code)
        assertNotNull(lyricResponse?.lrc?.lyric)

        println("✓ 根据歌名获取歌词成功")
        println("  歌曲: $songName - $artistName")
        println("  歌词预览: ${lyricResponse?.lrc?.lyric?.take(100)}...")
    }

    @Test
    @DisplayName("测试根据歌词片段获取完整歌词")
    @Tag("integration")
    fun testGetLyricsByFragment() {
        // Given
        val lyricFragment = "刮风这天"

        // When
        val results = client.getLyricsByFragment(lyricFragment, limit = 3)

        // Then
        assertNotNull(results)
        assertTrue(results.isNotEmpty())

        results.forEach { result ->
            assertNotNull(result.song)
            assertNotNull(result.lyric)
            assertEquals(200, result.lyric.code)
            assertNotNull(result.lyric.lrc?.lyric)
        }

        println("✓ 根据歌词片段获取完整歌词成功")
        println("  搜索片段: $lyricFragment")
        println("  找到 ${results.size} 首歌曲:")
        results.forEach { result ->
            val artists = result.song.artists.joinToString(", ") { it.name }
            println("    - ${result.song.name} - $artists")
            println("      歌词预览: ${result.lyric.lrc?.lyric?.take(50)}...")
        }
    }
}
