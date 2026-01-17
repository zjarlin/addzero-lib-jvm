package site.addzero.network.call.suno

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import site.addzero.network.call.suno.log.FileSunoLogStrategy
import site.addzero.network.call.suno.log.SunoLogStrategy
import site.addzero.network.call.suno.model.*
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * VectorEngine Suno API 客户端
 *
 * @param apiKey API 访问令牌
 * @param baseUrl API 基础 URL，默认为 VectorEngine 官方地址
 * @param logStrategy 日志记录策略
 */
class SunoClient(
  private val apiKey: String,
  private val baseUrl: String = "https://api.vectorengine.ai",
  private var logStrategy: SunoLogStrategy = FileSunoLogStrategy(),
) {

  fun getApiKey() = apiKey
  fun getBaseUrl() = baseUrl

  fun setLogStrategy(strategy: SunoLogStrategy) {
    this.logStrategy = strategy
  }

  private val client = OkHttpClient.Builder()
    .connectTimeout(30, TimeUnit.SECONDS)
    .readTimeout(60, TimeUnit.SECONDS)
    .writeTimeout(60, TimeUnit.SECONDS)
    .build()

  private val json = Json {
    ignoreUnknownKeys = true
    coerceInputValues = true
    encodeDefaults = true
  }

  /**
   * 提交生成音乐请求（统一入口）
   */
  fun submitMusic(request: SunoSubmitRequest): String {
    val bizName = when (request) {
      is SunoSubmitRequest.Inspiration -> "generate_inspiration"
      is SunoSubmitRequest.Custom -> "generate_custom"
      is SunoSubmitRequest.Extend -> "extend_music"
      is SunoSubmitRequest.ArtistConsistency -> "artist_consistency"
      is SunoSubmitRequest.Remix -> "remix_music"
      is SunoSubmitRequest.Concat -> "concat_songs"
    }
    val response = post<SunoTask>("/suno/submit/music", request, bizName)
    return response.id ?: throw RuntimeException("提交任务失败: 未返回 ID")
  }

  /**
   * 生成音乐（灵感模式）
   */
  fun generateMusicInspiration(
    description: String,
    instrumental: Boolean = false,
    model: String = "chirp-v5",
  ): String {
    return submitMusic(
      SunoSubmitRequest.Inspiration(
        gptDescriptionPrompt = description,
        makeInstrumental = instrumental,
        mv = model
      )
    )
  }

  /**
   * 生成音乐（自定义模式）
   */
  fun generateMusicCustom(
    prompt: String,
    title: String = "",
    tags: String = "",
    model: String = "chirp-v5",
    instrumental: Boolean = false,
  ): String {
    return submitMusic(
      SunoSubmitRequest.Custom(
        prompt = prompt,
        title = title,
        tags = tags,
        mv = model,
        makeInstrumental = instrumental
      )
    )
  }

  /**
   * 扩展现有音乐
   */
  fun extendMusic(
    clipId: String,
    continueAt: Int,
    prompt: String = "",
    title: String = "",
    tags: String = "",
    model: String = "chirp-v5",
  ): String {
    return submitMusic(
      SunoSubmitRequest.Extend(
        continueClipId = clipId,
        continueAt = continueAt,
        prompt = prompt,
        title = title,
        tags = tags,
        mv = model
      )
    )
  }

  /**
   * 歌手风格模式 (Persona)
   */
  fun artistConsistency(
    personaId: String,
    artistClipId: String,
    vocalGender: String? = null,
    generationType: String = "TEXT",
    negativeTags: String? = null,
    mv: String = "chirp-v4-tau",
  ): String {
    return submitMusic(
      SunoSubmitRequest.ArtistConsistency(
        personaId = personaId,
        artistClipId = artistClipId,
        vocalGender = vocalGender,
        generationType = generationType,
        negativeTags = negativeTags,
        mv = mv
      )
    )
  }

  /**
   * 二次创作/上传模式
   */
  fun remixMusic(
    clipId: String,
    prompt: String = "",
    title: String = "",
    tags: String = "",
    model: String = "chirp-v5",
  ): String {
    return submitMusic(
      SunoSubmitRequest.Remix(
        continueClipId = clipId,
        prompt = prompt,
        title = title,
        tags = tags,
        mv = model
      )
    )
  }

  /**
   * 拼接歌曲
   */
  fun concatSongs(clipId: String, isInfill: Boolean = false): String {
    return submitMusic(
      SunoSubmitRequest.Concat(
        clipId = clipId,
        isInfill = isInfill
      )
    )
  }

  /**
   * 生成歌词
   */
  fun generateLyrics(prompt: String): String {
    val request = GenerateLyricsRequest(prompt)
    val response = post<SunoTask>("/suno/submit/lyrics", request, "generate_lyrics")
    return response.prompt ?: throw RuntimeException("提交生成歌词任务失败")
  }

  /**
   * 获取单个任务信息
   */
  fun fetchTask(taskId: String): SunoTask? {
    val request = Request.Builder()
      .url("$baseUrl/suno/fetch/$taskId")
      .header("Authorization", "Bearer $apiKey")
      .get()
      .build()

    client.newCall(request).execute().use { response ->
      val bodyString = response.body?.string() ?: throw IOException("Empty response")

      // 记录日志
      logStrategy.log(
        bizName = "fetch_task_$taskId",
        requestBodyString = taskId,
        responseString = bodyString
      )

      if (!response.isSuccessful) {
        throw RuntimeException("请求失败: ${response.code} - $bodyString")
      }

      val apiResponse = try {
        json.decodeFromString<VectorEngineResponse<SunoTask>>(bodyString)
      } catch (e: Exception) {
        throw RuntimeException("解析响应失败: ${e.message}, body: $bodyString", e)
      }

      if (apiResponse.code != "success") {
        throw RuntimeException("API 错误: ${apiResponse.code} - ${apiResponse.message}")
      }

      return apiResponse.data
    }
  }

  /**
   * 批量获取任务信息
   */
  fun batchFetchTasks(taskIds: List<String>): List<SunoTask> {
    val request = BatchFetchRequest(taskIds)
    val response = post<List<SunoTask>>("/suno/fetch", request, "batch_fetch_tasks")
    return response ?: emptyList()
  }

  /**
   * 轮询等待任务完成
   */
  fun waitForCompletion(
    taskId: String,
    maxWaitTimeSeconds: Int = 600,
    pollIntervalSeconds: Int = 10,
    onStatusUpdate: ((String?) -> Unit)? = null,
  ): SunoTask {
    val startTime = System.currentTimeMillis()
    val maxWaitTimeMillis = maxWaitTimeSeconds * 1000L

    while (System.currentTimeMillis() - startTime < maxWaitTimeMillis) {
      val task = fetchTask(taskId)
      onStatusUpdate?.invoke(task?.status)

      if (task?.status == "complete" || task?.status == "streaming" || task?.status == "SUCCESS") {
        return task
      }

      if (task?.status == "error" || task?.status == "failed") {
        throw RuntimeException("任务失败: ${task.errorMessage ?: "未知错误"}")
      }

      Thread.sleep(pollIntervalSeconds * 1000L)
    }

    throw RuntimeException("等待任务超时: $taskId")
  }

  /**
   * 等待多个任务完成
   */
  fun waitForBatchCompletion(
    taskIds: List<String>,
    maxWaitTimeSeconds: Int = 600,
    pollIntervalSeconds: Int = 10,
  ): List<SunoTask> {
    val startTime = System.currentTimeMillis()
    val maxWaitTimeMillis = maxWaitTimeSeconds * 1000L
    val completedTasks = mutableMapOf<String, SunoTask>()

    while (System.currentTimeMillis() - startTime < maxWaitTimeMillis) {
      val remainingIds = taskIds.filter { !completedTasks.containsKey(it) }
      if (remainingIds.isEmpty()) break

      val tasks = batchFetchTasks(remainingIds)
      for (task in tasks) {
        val id = task.id ?: continue
        if (task.status == "complete" || task.status == "streaming" || task.status == "SUCCESS") {
          completedTasks[id] = task
        } else if (task.status == "error" || task.status == "failed") {
          throw RuntimeException("任务 ${task.id} 失败: ${task.errorMessage ?: "未知错误"}")
        }
      }

      if (completedTasks.size < taskIds.size) {
        Thread.sleep(pollIntervalSeconds * 1000L)
      }
    }

    return taskIds.mapNotNull { completedTasks[it] }
  }

  /**
   * 发送 POST 请求
   */
  private inline fun <reified T> post(path: String, body: Any, logBizName: String): T {
    val bodyString = when (body) {
      is SunoSubmitRequest -> json.encodeToString(body)
      is GenerateLyricsRequest -> json.encodeToString(body)
      is BatchFetchRequest -> json.encodeToString(body)
      else -> body.toString()
    }

    val requestBody = bodyString.toRequestBody("application/json".toMediaType())
    val url = if (path.startsWith("http")) path else if (baseUrl.endsWith("/") || path.startsWith("/")) "$baseUrl$path" else "$baseUrl/$path"

    val request = Request.Builder()
      .url(url)
      .header("Authorization", "Bearer $apiKey")
      .post(requestBody)
      .build()

    client.newCall(request).execute().use { response ->
      val responseString = response.body?.string() ?: throw IOException("Empty response")

      // 记录日志
      logStrategy.log(
        bizName = logBizName,
        requestBodyString = bodyString,
        responseString = responseString
      )

      if (!response.isSuccessful) {
        throw RuntimeException("请求失败: ${response.code} - $responseString")
      }

      val apiResponse = try {
        json.decodeFromString<VectorEngineResponse<T>>(responseString)
      } catch (e: Exception) {
        throw RuntimeException("解析响应失败: ${e.message}, body: $responseString", e)
      }

      if (apiResponse.code != "success") {
        throw RuntimeException("API 错误: ${apiResponse.code} - ${apiResponse.message}")
      }

      return apiResponse.data ?: throw RuntimeException("响应数据为空")
    }
  }
}

/**
 * Suno API 全局 entry point，支持动态配置
 */
object Suno {
  var apiKey: String = System.getenv("SUNO_API_TOKEN") ?: ""
  var baseUrl: String = "https://api.vectorengine.ai"
  private var logStrategy: SunoLogStrategy = FileSunoLogStrategy()

  private var _cachedClient: SunoClient? = null

  val client: SunoClient
    get() {
      val currentClient = _cachedClient
      if (currentClient == null || currentClient.getApiKey() != apiKey || currentClient.getBaseUrl() != baseUrl) {
        _cachedClient = SunoClient(apiKey, baseUrl, logStrategy)
      }
      return _cachedClient!!
    }

  fun setLogStrategy(strategy: SunoLogStrategy) {
    this.logStrategy = strategy
    _cachedClient = null // 强制重写 client 以应用新策略
  }

  /**
   * 便捷方法：直接设置配置并触发客户端重载
   */
  fun config(apiKey: String, baseUrl: String = "https://api.vectorengine.ai") {
    this.apiKey = apiKey
    this.baseUrl = baseUrl
    _cachedClient = null
  }

  // --- 代理 SunoClient 的所有主要方法 ---

  fun submitMusic(request: SunoSubmitRequest) = client.submitMusic(request)

  fun generateMusicInspiration(description: String, instrumental: Boolean = false, model: String = "chirp-v5") =
    client.generateMusicInspiration(description, instrumental, model)

  fun generateMusicCustom(prompt: String, title: String = "", tags: String = "", model: String = "chirp-v5", instrumental: Boolean = false) =
    client.generateMusicCustom(prompt, title, tags, model, instrumental)

  fun extendMusic(clipId: String, continueAt: Int, prompt: String = "", title: String = "", tags: String = "", model: String = "chirp-v5") =
    client.extendMusic(clipId, continueAt, prompt, title, tags, model)

  fun generateLyrics(prompt: String) = client.generateLyrics(prompt)

  fun concatSongs(clipId: String, isInfill: Boolean = false) = client.concatSongs(clipId, isInfill)

  fun fetchTask(taskId: String) = client.fetchTask(taskId)

  fun batchFetchTasks(taskIds: List<String>) = client.batchFetchTasks(taskIds)

  fun waitForCompletion(taskId: String, maxWaitTimeSeconds: Int = 600, pollIntervalSeconds: Int = 10, onStatusUpdate: ((String?) -> Unit)? = null) =
    client.waitForCompletion(taskId, maxWaitTimeSeconds, pollIntervalSeconds, onStatusUpdate)

  fun waitForBatchCompletion(taskIds: List<String>, maxWaitTimeSeconds: Int = 600, pollIntervalSeconds: Int = 10) =
    client.waitForBatchCompletion(taskIds, maxWaitTimeSeconds, pollIntervalSeconds)
}
