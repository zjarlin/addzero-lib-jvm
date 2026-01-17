package site.addzero.network.call.suno

import org.junit.jupiter.api.Test
import site.addzero.network.call.suno.model.SunoSubmitRequest

/**
 * VectorEngine Suno 客户端测试示例
 */
class SunoClientTest {
  @Test
  fun testSunoClient() {
    // 1. 动态配置示例 (可选，默认从环境变量读取)
    val apiToken = System.getenv("SUNO_API_TOKEN")
      ?: "dummy-token-for-demo"

    Suno.config(
      apiKey = apiToken,
      baseUrl = "https://api.vectorengine.ai"
    )

    // 2. 使用全局单例 Suno (灵感模式)
    println("=== 示例 1: 灵感模式 (使用单例) ===")
    try {
      val taskId1 = Suno.generateMusicInspiration(
        description = "一首关于春天的欢快流行歌",
        instrumental = false,
        model = "chirp-v5"
      )
      println("任务 ID: $taskId1")
    } catch (e: Exception) {
      println("提交失败 (可能由于 token 无效): ${e.message}")
    }

    // 3. 自定义模式示例
    println("\n=== 示例 2: 自定义模式 ===")
    val lyrics = """
        [Verse]
        春天来了，花儿开了
        阳光明媚，心情愉快
        
        [Chorus]
        让我们一起歌唱
        迎接美好的春天
    """.trimIndent()

    // 使用统一提交接口演示
    val customReq = SunoSubmitRequest.Custom(
      prompt = lyrics,
      title = "春天的歌",
      tags = "pop, cheerful, spring",
      mv = "chirp-v5"
    )

    try {
      val taskId2 = Suno.submitMusic(customReq)
      println("任务 ID: $taskId2")

      // 4. 等待任务完成演示
      println("正在等待任务完成...")
      val result = Suno.waitForCompletion(taskId2, 600, 10) { status ->
        println("状态更新: $status")
      }
      println("标题: ${result.title}")
      println("音频 URL: ${result.audioUrl}")
    } catch (e: Exception) {
      println("演示失败: ${e.message}")
    }
  }
}
