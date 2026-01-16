package site.addzero.tool.music.design

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Tag

/**
 * 音乐设计工具类测试
 */
@DisplayName("音乐设计工具类测试")
class MusicDesignUtilTest {

    @Test
    @DisplayName("测试预览歌曲歌词")
    @Tag("integration")
    fun testPreviewSongLyrics() {
        // When
        val result = MusicDesignUtil.previewSongLyrics("晴天", "周杰伦")

        // Then
        assertNotNull(result)
        assertNotNull(result?.song)
        assertNotNull(result?.lyric?.lrc?.lyric)

        println("✓ 预览歌词成功")
        println("  歌曲: ${result?.song?.name}")
        println("  歌手: ${result?.song?.artists?.joinToString { it.name }}")
        println("  歌词预览: ${result?.lyric?.lrc?.lyric?.take(100)}...")
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
//      sk-gq6Yem14Vl5BP8RIlW8eLjeZu1mH3PID4o7v6PZPWu0JA3kB

        // When
        val taskId = MusicDesignUtil.remixBySongName(songName, artistName, tags)

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
        val taskId = MusicDesignUtil.remixByLyricFragment(lyricFragment, tags)

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
        val taskId = MusicDesignUtil.createMusicWithLyrics(lyrics, title, tags)

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
            "你要的全拿走" to "胡彦斌"
        )
        val tags = "pop, chinese,黑人福音"

        // When
        val taskIds = MusicDesignUtil.batchRemixBySongNames(songInfos, tags)

        // Then
        assertNotNull(taskIds)
        assertTrue(taskIds.isNotEmpty())

        println("✓ 批量生成音乐任务创建成功")
        println("  创建了 ${taskIds.size} 个任务")
        taskIds.forEachIndexed { index, taskId ->
            println("    ${index + 1}. $taskId")
        }
    }

     @Test
    @DisplayName("测试查看单个任务")
    @Tag("integration")
    @Tag("suno")
    fun testFetchTask() {
        // Given - 先创建一个任务
        val taskId = MusicDesignUtil.remixBySongName("晴天", "周杰伦", "pop, chinese")
        assertNotNull(taskId)

        // When - 查看任务状态
        val task = MusicDesignUtil.fetchTask(taskId!!)

        // Then
        assertNotNull(task)

        println("✓ 查看任务成功")
        println("  任务 ID: ${task?.id}")
        println("  状态: ${task?.status}")
        println("  标题: ${task?.title}")
        if (task?.audioUrl != null) {
            println("  音频 URL: ${task.audioUrl}")
        }
    }

    @Test
    @DisplayName("测试批量查看任务")
    @Tag("integration")
    @Tag("suno")
    fun testBatchFetchTasks() {
        // Given - 创建多个任务
        val songInfos = listOf(
            "晴天" to "周杰伦",
            "稻香" to "周杰伦"
        )
        val taskIds = MusicDesignUtil.batchRemixBySongNames(songInfos, "pop, chinese")
        assertTrue(taskIds.isNotEmpty())

        // When - 批量查看任务
        val tasks = MusicDesignUtil.batchFetchTasks(taskIds)

        // Then
        assertNotNull(tasks)
        assertEquals(taskIds.size, tasks.size)

        println("✓ 批量查看任务成功")
        println("  查询了 ${tasks.size} 个任务:")
        tasks.forEach { task ->
            println("    - ${task.id}: ${task.status} - ${task.title}")
        }
    }

    @Test
    @DisplayName("测试创建任务后轮询查看状态")
    @Tag("integration")
    @Tag("suno")
    fun testCreateAndPollTask() {
        // Given
        val songName = "早点早点"
        val artistName = "沙一汀"

        // When - 创建任务
        val taskId = MusicDesignUtil.remixBySongName(songName, artistName, "pop, chinese, 黑人福音")
        assertNotNull(taskId)
        println("✓ 任务已创建: $taskId")

        // Then - 轮询查看状态（最多 3 次）
        repeat(3) { index ->
            Thread.sleep(5000) // 等待 5 秒
            val task = MusicDesignUtil.fetchTask(taskId!!)
            println("  第 ${index + 1} 次查询 - 状态: ${task?.status}")

            if (task?.status == "complete" || task?.status == "streaming") {
                println("  ✓ 任务完成!")
                println("    音频: ${task.audioUrl}")
                println("    视频: ${task.videoUrl}")
                return
            }
        }

        println("  任务仍在处理中...")
    }

}


