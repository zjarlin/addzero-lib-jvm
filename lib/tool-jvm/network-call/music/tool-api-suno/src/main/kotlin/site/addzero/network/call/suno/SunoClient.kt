package site.addzero.network.call.suno

import com.alibaba.fastjson2.JSON
import com.alibaba.fastjson2.TypeReference
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import site.addzero.network.call.suno.model.*
import java.util.concurrent.TimeUnit

/**
 * VectorEngine Suno API 客户端
 * 
 * @param apiToken API 访问令牌
 * @param baseUrl API 基础 URL，默认为 VectorEngine 官方地址
 */
class SunoClient(
    private val apiToken: String,
    private val baseUrl: String = "https://api.vectorengine.ai"
) {
    
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()
    
    companion object {
        private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()
    }
    
    /**
     * 生成音乐（灵感模式）
     * 
     * @param request 生成请求
     * @return 任务 ID
     */
    fun generateMusicInspiration(request: GenerateMusicInspirationRequest): String {
        val response = post<String>("$baseUrl/suno/submit/music", request)
        return response.data ?: throw RuntimeException("未返回任务 ID")
    }
    
    /**
     * 生成音乐（灵感模式）- 简化版
     * 
     * @param description 音乐描述
     * @param instrumental 是否为纯音乐（无人声）
     * @param model 模型版本，默认 chirp-v5
     * @param customPrompt 自定义提示词
     * @return 任务 ID
     */
    fun generateMusicInspiration(
        description: String,
        instrumental: Boolean = false,
        model: String = "chirp-v5",
        customPrompt: String = ""
    ): String {
        val request = GenerateMusicInspirationRequest(
            gptDescriptionPrompt = description,
            makeInstrumental = instrumental,
            mv = model,
            prompt = customPrompt
        )
        return generateMusicInspiration(request)
    }
    
    /**
     * 生成音乐（自定义模式）
     * 
     * @param request 生成请求
     * @return 任务 ID
     */
    fun generateMusicCustom(request: GenerateMusicCustomRequest): String {
        val response = post<String>("$baseUrl/suno/submit/music", request)
        return response.data ?: throw RuntimeException("未返回任务 ID")
    }
    
    /**
     * 生成音乐（自定义模式）- 简化版
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
        val request = GenerateMusicCustomRequest(
            prompt = lyrics,
            mv = model,
            title = title,
            tags = tags
        )
        return generateMusicCustom(request)
    }
    
    /**
     * 扩展音乐（续写模式）
     * 
     * @param request 扩展请求
     * @return 任务 ID
     */
    fun extendMusic(request: ExtendMusicRequest): String {
        val response = post<String>("$baseUrl/suno/submit/music", request)
        return response.data ?: throw RuntimeException("未返回任务 ID")
    }
    
    /**
     * 扩展音乐 - 简化版
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
        val request = ExtendMusicRequest(
            prompt = lyrics,
            mv = model,
            title = title,
            tags = tags,
            continueAt = continueAt,
            continueClipId = clipId,
            task = "extend"
        )
        return extendMusic(request)
    }
    
    /**
     * 生成歌词
     * 
     * @param prompt 歌词描述提示词
     * @return 生成的歌词文本
     */
    fun generateLyrics(prompt: String): String {
        val request = GenerateLyricsRequest(prompt)
        val response = post<String>("$baseUrl/suno/lyrics", request)
        return response.data ?: throw RuntimeException("未返回歌词")
    }
    
    /**
     * 拼接歌曲
     * 
     * @param clipId 音频片段 ID
     * @return 任务 ID
     */
    fun concatSongs(clipId: String): String {
        val request = ConcatSongsRequest(clipId)
        val response = post<String>("$baseUrl/suno/concat", request)
        return response.data ?: throw RuntimeException("未返回任务 ID")
    }
    
    /**
     * 获取单个任务信息
     * 
     * @param taskId 任务 ID
     * @return 任务信息，如果不存在返回 null
     */
    fun fetchTask(taskId: String): SunoTask? {
        val url = "$baseUrl/suno/fetch/$taskId"
        val httpRequest = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $apiToken")
            .addHeader("Accept", "application/json")
            .get()
            .build()
        
        client.newCall(httpRequest).execute().use { response ->
            val responseBody = response.body?.string()
                ?: throw RuntimeException("响应为空")
            
            if (!response.isSuccessful) {
                throw RuntimeException("请求失败: ${response.code} - $responseBody")
            }
            
            val result = JSON.parseObject(responseBody, object : TypeReference<VectorEngineResponse<SunoTask>>() {})
            if (result.code != "success") {
                throw RuntimeException("API 错误: ${result.message}")
            }
            
            return result.data
        }
    }
    
    /**
     * 批量获取任务信息
     * 
     * @param taskIds 任务 ID 列表
     * @return 任务信息列表
     */
    fun batchFetchTasks(taskIds: List<String>): List<SunoTask> {
        val request = BatchFetchRequest(taskIds)
        val response = post<List<SunoTask>>("$baseUrl/suno/fetch", request)
        return response.data ?: emptyList()
    }
    
    /**
     * 等待任务完成
     * 
     * @param taskId 任务 ID
     * @param maxWaitTimeSeconds 最长等待时间（秒），默认 600 秒
     * @param pollIntervalSeconds 轮询间隔（秒），默认 10 秒
     * @param onStatusUpdate 状态更新回调
     * @return 完成的任务信息
     * @throws RuntimeException 任务失败或超时
     */
    fun waitForCompletion(
        taskId: String,
        maxWaitTimeSeconds: Int = 600,
        pollIntervalSeconds: Int = 10,
        onStatusUpdate: ((String?) -> Unit)? = null
    ): SunoTask {
        val startTime = System.currentTimeMillis()
        val maxWaitMillis = maxWaitTimeSeconds * 1000L
        
        while (System.currentTimeMillis() - startTime < maxWaitMillis) {
            val task = fetchTask(taskId)
            onStatusUpdate?.invoke(task?.status)
            
            when (task?.status) {
                "complete", "streaming" -> {
                    return task
                }
                "error" -> {
                    throw RuntimeException("任务失败: ${task.error}")
                }
                else -> {
                    Thread.sleep(pollIntervalSeconds * 1000L)
                }
            }
        }
        
        throw RuntimeException("任务超时，已等待 $maxWaitTimeSeconds 秒")
    }
    
    /**
     * 等待多个任务完成
     * 
     * @param taskIds 任务 ID 列表
     * @param maxWaitTimeSeconds 最长等待时间（秒），默认 600 秒
     * @param pollIntervalSeconds 轮询间隔（秒），默认 10 秒
     * @return 完成的任务信息列表
     * @throws RuntimeException 任何任务失败或超时
     */
    fun waitForBatchCompletion(
        taskIds: List<String>,
        maxWaitTimeSeconds: Int = 600,
        pollIntervalSeconds: Int = 10
    ): List<SunoTask> {
        val startTime = System.currentTimeMillis()
        val maxWaitMillis = maxWaitTimeSeconds * 1000L
        
        while (System.currentTimeMillis() - startTime < maxWaitMillis) {
            val tasks = batchFetchTasks(taskIds)
            val allComplete = tasks.all { it.status == "complete" || it.status == "streaming" }
            
            if (allComplete) {
                return tasks
            }
            
            val anyError = tasks.any { it.status == "error" }
            if (anyError) {
                val errorTasks = tasks.filter { it.status == "error" }
                throw RuntimeException("部分任务失败: ${errorTasks.map { it.error }}")
            }
            
            Thread.sleep(pollIntervalSeconds * 1000L)
        }
        
        throw RuntimeException("任务超时，已等待 $maxWaitTimeSeconds 秒")
    }
    
    private inline fun <reified T> post(url: String, body: Any): VectorEngineResponse<T> {
        val jsonBody = JSON.toJSONString(body)
        val requestBody = jsonBody.toRequestBody(JSON_MEDIA_TYPE)
        
        val httpRequest = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $apiToken")
            .addHeader("Accept", "application/json")
            .addHeader("Content-Type", "application/json")
            .post(requestBody)
            .build()
        
        client.newCall(httpRequest).execute().use { response ->
            val responseBody = response.body?.string()
                ?: throw RuntimeException("响应为空")
            
            if (!response.isSuccessful) {
                throw RuntimeException("请求失败: ${response.code} - $responseBody")
            }
            
            val result = JSON.parseObject(responseBody, object : TypeReference<VectorEngineResponse<T>>() {})
            if (result.code != "success") {
                throw RuntimeException("API 错误: ${result.message}")
            }
            
            return result
        }
    }
}
