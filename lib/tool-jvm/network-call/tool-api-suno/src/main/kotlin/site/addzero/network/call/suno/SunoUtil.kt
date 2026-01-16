package site.addzero.network.call.suno

import site.addzero.network.call.suno.model.*

/**
 * VectorEngine Suno API 工具类
 * 自动从环境变量读取 API Token
 */
object SunoUtil {
    
    private val client: SunoClient by lazy {
        val token = System.getenv("SUNO_API_TOKEN")
            ?: throw IllegalStateException("未设置环境变量 SUNO_API_TOKEN")
        SunoClient(token)
    }
    
    /**
     * 生成音乐（灵感模式）
     * 
     * @param description 音乐描述
     * @param instrumental 是否为纯音乐
     * @param model 模型版本，默认 chirp-v5
     * @return 任务 ID
     */
    fun generateMusicInspiration(
        description: String,
        instrumental: Boolean = false,
        model: String = "chirp-v5"
    ): String {
        return client.generateMusicInspiration(description, instrumental, model)
    }
    
    /**
     * 生成音乐（自定义模式，带歌词）
     * 
     * @param lyrics 歌词内容
     * @param title 歌曲标题
     * @param tags 音乐风格标签
     * @param model 模型版本，默认 chirp-v5
     * @return 任务 ID
     */
    fun generateMusicCustom(
        lyrics: String,
        title: String = "",
        tags: String = "",
        model: String = "chirp-v5"
    ): String {
        return client.generateMusicCustom(lyrics, title, tags, model)
    }
    
    /**
     * 扩展现有音乐
     * 
     * @param clipId 原音频片段 ID
     * @param continueAt 从第几秒开始续写
     * @param lyrics 续写的歌词
     * @param title 歌曲标题
     * @param tags 音乐风格标签
     * @param model 模型版本，默认 chirp-v5
     * @return 任务 ID
     */
    fun extendMusic(
        clipId: String,
        continueAt: Int,
        lyrics: String = "",
        title: String = "",
        tags: String = "",
        model: String = "chirp-v5"
    ): String {
        return client.extendMusic(clipId, continueAt, lyrics, title, tags, model)
    }
    
    /**
     * 生成歌词
     * 
     * @param prompt 歌词描述提示词
     * @return 生成的歌词文本
     */
    fun generateLyrics(prompt: String): String {
        return client.generateLyrics(prompt)
    }
    
    /**
     * 拼接歌曲
     * 
     * @param clipId 音频片段 ID
     * @return 任务 ID
     */
    fun concatSongs(clipId: String): String {
        return client.concatSongs(clipId)
    }
    
    /**
     * 获取单个任务信息
     * 
     * @param taskId 任务 ID
     * @return 任务信息
     */
    fun fetchTask(taskId: String): SunoTask? {
        return client.fetchTask(taskId)
    }
    
    /**
     * 批量获取任务信息
     * 
     * @param taskIds 任务 ID 列表
     * @return 任务信息列表
     */
    fun batchFetchTasks(taskIds: List<String>): List<SunoTask> {
        return client.batchFetchTasks(taskIds)
    }
    
    /**
     * 等待任务完成
     * 
     * @param taskId 任务 ID
     * @param maxWaitTimeSeconds 最长等待时间（秒）
     * @param pollIntervalSeconds 轮询间隔（秒）
     * @param onStatusUpdate 状态更新回调
     * @return 完成的任务信息
     */
    fun waitForCompletion(
        taskId: String,
        maxWaitTimeSeconds: Int = 600,
        pollIntervalSeconds: Int = 10,
        onStatusUpdate: ((String?) -> Unit)? = null
    ): SunoTask {
        return client.waitForCompletion(taskId, maxWaitTimeSeconds, pollIntervalSeconds, onStatusUpdate)
    }
    
    /**
     * 等待多个任务完成
     * 
     * @param taskIds 任务 ID 列表
     * @param maxWaitTimeSeconds 最长等待时间（秒）
     * @param pollIntervalSeconds 轮询间隔（秒）
     * @return 完成的任务信息列表
     */
    fun waitForBatchCompletion(
        taskIds: List<String>,
        maxWaitTimeSeconds: Int = 600,
        pollIntervalSeconds: Int = 10
    ): List<SunoTask> {
        return client.waitForBatchCompletion(taskIds, maxWaitTimeSeconds, pollIntervalSeconds)
    }
    
    /**
     * 生成音乐（灵感模式）并等待完成
     * 
     * @param description 音乐描述
     * @param instrumental 是否为纯音乐
     * @param model 模型版本，默认 chirp-v5
     * @param maxWaitTimeSeconds 最长等待时间（秒）
     * @return 完成的任务信息
     */
    fun generateMusicInspirationAndWait(
        description: String,
        instrumental: Boolean = false,
        model: String = "chirp-v5",
        maxWaitTimeSeconds: Int = 600
    ): SunoTask {
        val taskId = generateMusicInspiration(description, instrumental, model)
        return waitForCompletion(taskId, maxWaitTimeSeconds)
    }
    
    /**
     * 生成音乐（自定义模式）并等待完成
     * 
     * @param lyrics 歌词内容
     * @param title 歌曲标题
     * @param tags 音乐风格标签
     * @param model 模型版本，默认 chirp-v5
     * @param maxWaitTimeSeconds 最长等待时间（秒）
     * @return 完成的任务信息
     */
    fun generateMusicCustomAndWait(
        lyrics: String,
        title: String = "",
        tags: String = "",
        model: String = "chirp-v5",
        maxWaitTimeSeconds: Int = 600
    ): SunoTask {
        val taskId = generateMusicCustom(lyrics, title, tags, model)
        return waitForCompletion(taskId, maxWaitTimeSeconds)
    }
}
