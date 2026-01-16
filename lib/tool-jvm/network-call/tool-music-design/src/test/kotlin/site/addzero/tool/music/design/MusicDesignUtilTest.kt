package site.addzero.tool.music.design

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.BeforeEach

/**
 * 音乐设计工具类测试
 */
@DisplayName("音乐设计工具类测试")
class MusicDesignUtilTest {
    
    private lateinit var musicDesign: MusicDesignUtil
    
    @BeforeEach
    fun setup() {
        // 从环境变量读取 API Token
        musicDesign = MusicDesignUtil()
    }
    
    @Test
    @DisplayName("测试预览歌曲歌词")
    @Tag("integration")
    fun testPreviewSongLyrics() {
        // When
        val result = MusicDesignUtil.previewSongLyrics("晴天", "周杰伦")
        
        // Then
        assertNotNull(result)
        assertNotNull(result?.song)
        assertNotNull(result?.lyric.lrc?.lyric)
        
        println("✓ 预览歌词成功")
        println("  歌曲: ${result?.song?.name}")
        println("  歌手: ${result?.song?.artists?.joinToString { it.name }}")
        println("  歌词预览: ${result?.lyric.lrc?.lyric?.take(100)}...")
    }
    
    @Test
    @DisplayName("测试根据歌词片段预览")
    @Tag("integration")
    fun testPreviewByLyricFragment() {
        // When
        val results = MusicDesignUtil.previewByLyricFragment("刮风这天我试过握着你手", limit = 3)
        
        // Then
        assertNotNull(results)
        assertTrue(results.isNotEmpty())
        
        println("✓ 根据歌词片段预览成功")
        println("  找到 ${results.size} 首歌曲:")
        results.forEach { result ->
            println("    - ${result.song.name} - ${result.song.artists.joinToString { it.name }}")
        }
    }
    
    @Test
    @DisplayName("测试根据歌名生成音乐")
    @Tag("integration")
    @Tag("suno")
    fun testRemixBySongName() {
        // Given
        val songName = "晴天"
        val artistName = "周杰伦"
        val tags = "pop, chinese"
        
        // When
        val taskId = musicDesign.remixBySongName(songName, artistName, tags)
        
        // Then
        assertNotNull(taskId)
        assertTrue(taskId!!.isNotBlank())
        
        println("✓ 根据歌名生成音乐任务创建成功")
        println("  任务 ID: $taskId")
    }
    
    @Test
    @DisplayName("测试根据歌词片段生成音乐")
    @Tag("integration")
    @Tag("suno")
    fun testRemixByLyricFragment() {
        // Given
        val lyricFragment = "刮风这天我试过握着你手"
        val tags = "ballad, chinese"
        
        // When
        val taskId = musicDesign.remixByLyricFragment(lyricFragment, tags)
        
        // Then
        assertNotNull(taskId)
        assertTrue(taskId!!.isNotBlank())
        
        println("✓ 根据歌词片段生成音乐任务创建成功")
        println("  任务 ID: $taskId")
    }
    
    @Test
    @DisplayName("测试使用自定义歌词生成音乐")
    @Tag("integration")
    @Tag("suno")
    fun testCreateMusicWithLyrics() {
        // Given
        val lyrics = """
            天空灰得像哭过
            离开你以后
            并没有更自由
            酸酸的空气
            嗅出我们的距离
        """.trimIndent()
        val title = "测试歌曲"
        val tags = "pop, sad"
        
        // When
        val taskId = musicDesign.createMusicWithLyrics(lyrics, title, tags)
        
        // Then
        assertNotNull(taskId)
        assertTrue(taskId.isNotBlank())
        
        println("✓ 使用自定义歌词生成音乐任务创建成功")
        println("  任务 ID: $taskId")
    }
    
    @Test
    @DisplayName("测试批量生成音乐")
    @Tag("integration")
    @Tag("suno")
    fun testBatchRemixBySongNames() {
        // Given
        val songInfos = listOf(
            "晴天" to "周杰伦",
            "稻香" to "周杰伦"
        )
        val tags = "pop, chinese"
        
        // When
        val taskIds = musicDesign.batchRemixBySongNames(songInfos, tags)
        
        // Then
        assertNotNull(taskIds)
        assertTrue(taskIds.isNotEmpty())
        
        println("✓ 批量生成音乐任务创建成功")
        println("  创建了 ${taskIds.size} 个任务")
        taskIds.forEachIndexed { index, taskId ->
            println("    ${index + 1}. $taskId")
        }
    }
}
