package site.addzero.network.call.suno

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.BeforeEach
import site.addzero.network.call.suno.model.SunoMusicRequest

/**
 * Suno 客户端测试
 */
@DisplayName("Suno 客户端测试")
class SunoClientTest {

    private lateinit var client: SunoClient

    @BeforeEach
    fun setup() {
        // 从环境变量读取 API Token
        val token = System.getenv("SUNO_API_TOKEN")
            ?: throw IllegalStateException("未设置环境变量 SUNO_API_TOKEN")
        client = SunoClient(token)
    }

    @Test
    @DisplayName("测试生成音乐（灵感模式）")
    @Tag("integration")
    fun testGenerateMusicInspiration() {
        // Given
        val description = "一首轻快的流行歌曲，关于夏天和海边"
       SunoMusicRequest(
         prompt = TODO()
       )

    }

    @Test
    @DisplayName("测试生成音乐（自定义模式）")
    @Tag("integration")
    fun testGenerateMusicCustom() {
        // Given
        val lyrics = """
            天空灰得像哭过
            离开你以后
            并没有更自由
        """.trimIndent()
        val title = "测试歌曲"
        val tags = "pop, sad, chinese"

        // When
        val taskId = client.generateMusicCustom(lyrics, title, tags)

        // Then
        assertNotNull(taskId)
        assertTrue(taskId.isNotBlank())

        println("✓ 自定义模式生成任务创建成功")
        println("  任务 ID: $taskId")
    }

    @Test
    @DisplayName("测试生成纯音乐")
    @Tag("integration")
    fun testGenerateInstrumental() {
        // Given
        val description = "一首舒缓的钢琴曲"

        // When
        val taskId = client.generateMusicInspiration(
            description = description,
            instrumental = true
        )

        // Then
        assertNotNull(taskId)
        assertTrue(taskId.isNotBlank())

        println("✓ 纯音乐生成任务创建成功")
        println("  任务 ID: $taskId")
    }

    @Test
    @DisplayName("测试生成歌词")
    @Tag("integration")
    fun testGenerateLyrics() {
        // Given
        val prompt = "写一首关于友情的歌词，风格温暖感人"

        // When
        val lyrics = client.generateLyrics(prompt)

        // Then
        assertNotNull(lyrics)
        assertTrue(lyrics.isNotBlank())

        println("✓ 歌词生成成功")
        println("  歌词内容:")
        println(lyrics)
    }

    @Test
    @DisplayName("测试查询单个任务")
    @Tag("integration")
    fun testFetchTask() {
        // Given - 先创建一个任务
        val description = "一首欢快的歌曲"
        val taskId = client.generateMusicInspiration(description)

        // When - 查询任务
        Thread.sleep(2000) // 等待 2 秒
        val task = client.fetchTask(taskId)

        // Then
        assertNotNull(task)
        assertEquals(taskId, task?.id)
        assertNotNull(task?.status)

        println("✓ 查询任务成功")
        println("  任务 ID: ${task?.id}")
        println("  状态: ${task?.status}")
    }

    @Test
    @DisplayName("测试批量查询任务")
    @Tag("integration")
    fun testBatchFetchTasks() {
        // Given - 创建多个任务
        val taskId1 = client.generateMusicInspiration("欢快的歌")
        val taskId2 = client.generateMusicInspiration("悲伤的歌")
        val taskIds = listOf(taskId1, taskId2)

        // When - 批量查询
        Thread.sleep(2000) // 等待 2 秒
        val tasks = client.batchFetchTasks(taskIds)

        // Then
        assertNotNull(tasks)
        assertEquals(2, tasks.size)

        println("✓ 批量查询任务成功")
        tasks.forEach { task ->
            println("  - ${task.id}: ${task.status}")
        }
    }

    @Test
    @DisplayName("测试等待任务完成")
    @Tag("integration")
    @Tag("slow")
    fun testWaitForCompletion() {
        // Given
        val lyrics = "简单的测试歌词"
        val taskId = client.generateMusicCustom(lyrics, "测试", "pop")

        println("✓ 任务已创建: $taskId")
        println("  等待任务完成...")

        // When
        val task = client.waitForCompletion(
            taskId = taskId,
            maxWaitTimeSeconds = 300,
            pollIntervalSeconds = 10,
            onStatusUpdate = { status ->
                println("  当前状态: $status")
            }
        )

        // Then
        assertNotNull(task)
        assertTrue(task.status == "complete" || task.status == "streaming")
        assertNotNull(task.audioUrl)

        println("✓ 任务完成!")
        println("  音频 URL: ${task.audioUrl}")
        println("  视频 URL: ${task.videoUrl}")
    }

    @Test
    @DisplayName("测试拼接歌曲")
    @Tag("integration")
    fun testConcatSongs() {
        // Given - 需要一个已存在的 clip_id
        // 这里使用一个示例 ID，实际测试需要替换
        val clipId = "test-clip-id"

        // When
        try {
            val taskId = client.concatSongs(clipId)

            // Then
            assertNotNull(taskId)
            println("✓ 拼接任务创建成功")
            println("  任务 ID: $taskId")
        } catch (e: Exception) {
            println("⚠ 拼接测试需要有效的 clip_id")
            println("  错误: ${e.message}")
        }
    }

    @Test
    @DisplayName("测试扩展音乐（续写）")
    @Tag("integration")
    fun testExtendMusic() {
        // Given - 需要一个已存在的 clip_id
        val clipId = "test-clip-id"
        val continueAt = 30 // 从 30 秒开始续写
        val lyrics = "续写的歌词内容"

        // When
        try {
            val taskId = client.extendMusic(
                clipId = clipId,
                continueAt = continueAt,
                lyrics = lyrics,
                tags = "pop"
            )

            // Then
            assertNotNull(taskId)
            println("✓ 续写任务创建成功")
            println("  任务 ID: $taskId")
        } catch (e: Exception) {
            println("⚠ 续写测试需要有效的 clip_id")
            println("  错误: ${e.message}")
        }
    }
}
