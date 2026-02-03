package site.addzero.network.call.suno

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import site.addzero.common.models.result.Result
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

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    companion object {
        private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()
    }

    /**
     * 生成音乐
     *
     * @param request 音乐生成请求
     * @return 任务 ID
     */
    fun generateMusic(
        request: SunoMusicRequest
    ): String {
      val jsonBody = json.encodeToString(request)
      val response = postWithBody("$baseUrl/suno/submit/music", jsonBody)
      println("gen music response: $response")
      val decodeFromString = json.decodeFromString<Result<String>>(response)
      return decodeFromString.getOrThrow()
    }

    /**
     * 生成歌词
     * 使用 AI 根据描述生成歌词
     *
     * @param prompt 歌词描述提示词
     * @return 生成的歌词文本
     */
    fun generateLyrics(prompt: String): String {
        val request = GenerateLyricsRequest(prompt)
        val response = postWithBody("$baseUrl/suno/lyrics", json.encodeToString(request))
        return json.decodeFromString<Result<String>>(response).getOrThrow()
    }

    /**
     * 拼接歌曲
     * 将音频片段拼接成完整歌曲
     *
     * @param clipId 音频片段 ID
     * @return 任务 ID
     */
    fun concatSongs(clipId: String): String {
        val request = ConcatSongsRequest(clipId)
        val response = postWithBody("$baseUrl/suno/concat", json.encodeToString(request))
        return json.decodeFromString<Result<String>>(response).getOrThrow()
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
          println("fetch suno task response: $responseBody")
            val decodeFromString = json.decodeFromString<Result<SunoTask>>(responseBody)
            return decodeFromString.getOrNull()
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
        val response = postWithBody("$baseUrl/suno/fetch", json.encodeToString(request))
        return json.decodeFromString<Result<List<SunoTask>>>(response).getOrDefault(emptyList())
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
                    throw RuntimeException("任务失败: ${task.error ?: task.errorMessage}")
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
                throw RuntimeException("部分任务失败: ${errorTasks.map { it.error ?: it.errorMessage }}")
            }

            Thread.sleep(pollIntervalSeconds * 1000L)
        }

        throw RuntimeException("任务超时，已等待 $maxWaitTimeSeconds 秒")
    }

    /**
     * 发送 POST 请求的底层方法
     */
    private fun postWithBody(url: String, jsonBody: String): String {
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

            return responseBody
        }
    }
}
