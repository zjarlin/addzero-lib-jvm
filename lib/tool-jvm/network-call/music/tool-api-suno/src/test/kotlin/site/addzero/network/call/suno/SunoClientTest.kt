package site.addzero.network.call.suno

import site.addzero.network.call.suno.model.*

/**
 * VectorEngine Suno 客户端测试示例
 */
fun main() {
    val apiToken = System.getenv("SUNO_API_TOKEN") 
        ?: throw IllegalStateException("未设置环境变量 SUNO_API_TOKEN")
    
    val client = SunoClient(apiToken)
    
    // 示例 1: 生成音乐（灵感模式）
    println("=== 示例 1: 生成音乐（灵感模式） ===")
    val taskId1 = client.generateMusicInspiration(
        description = "一首关于春天的欢快流行歌",
        instrumental = false,
        model = "chirp-v5"
    )
    println("任务 ID: $taskId1")
    
    val result1 = client.waitForCompletion(taskId1) { status ->
        println("状态: $status")
    }
    println("标题: ${result1.title}")
    println("音频 URL: ${result1.audioUrl}")
    println("时长: ${result1.duration}秒")
    
    // 示例 2: 生成音乐（自定义模式，带歌词）
    println("\n=== 示例 2: 生成音乐（自定义模式） ===")
    val lyrics = """
        [Verse]
        春天来了，花儿开了
        阳光明媚，心情愉快
        
        [Chorus]
        让我们一起歌唱
        迎接美好的春天
    """.trimIndent()
    
    val taskId2 = client.generateMusicCustom(
        lyrics = lyrics,
        title = "春天的歌",
        tags = "pop, cheerful, spring",
        model = "chirp-v5"
    )
    println("任务 ID: $taskId2")
    
    val result2 = client.waitForCompletion(taskId2)
    println("标题: ${result2.title}")
    println("音频 URL: ${result2.audioUrl}")
    
    // 示例 3: 扩展音乐
    println("\n=== 示例 3: 扩展音乐 ===")
    val extendTaskId = client.extendMusic(
        clipId = result2.id,
        continueAt = 60,
        lyrics = "[Verse 2]\n继续唱下去\n春天永不结束",
        title = "春天的歌 (扩展版)",
        tags = "pop, cheerful",
        model = "chirp-v5"
    )
    println("扩展任务 ID: $extendTaskId")
    
    // 示例 4: 生成歌词
    println("\n=== 示例 4: 生成歌词 ===")
    val lyricsText = client.generateLyrics("写一首关于夏天的歌词")
    println("生成的歌词:\n$lyricsText")
    
    // 示例 5: 批量获取任务
    println("\n=== 示例 5: 批量获取任务 ===")
    val tasks = client.batchFetchTasks(listOf(taskId1, taskId2))
    tasks.forEach { task ->
        println("${task.title}: ${task.status}")
    }
    
    // 示例 6: 使用 SunoUtil 简化 API
    println("\n=== 示例 6: 使用 SunoUtil ===")
    val task = SunoUtil.generateMusicInspirationAndWait(
        description = "一首轻松的爵士乐",
        instrumental = true,
        model = "chirp-v5"
    )
    println("${task.title}: ${task.audioUrl}")
}
