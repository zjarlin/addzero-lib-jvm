package site.addzero.kcloud.api.suno

import io.ktor.client.HttpClient
import io.ktor.client.plugins.*
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.content.OutgoingContent
import io.ktor.util.date.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import site.addzero.core.network.HttpClientFactory
import kotlin.math.roundToInt

internal expect fun buildSunoApi(
    baseUrl: String,
    httpClient: HttpClient,
): SunoApi

/**
 * Suno API 客户端（全量接口）
https://sunoapi.org/zh-CN/api-key
 * 对接 https://api.sunoapi.org/api/v1
 */
class SunoApiClient(
    private val apiToken: String = "",
    baseUrl: String = DEFAULT_BASE_URL,
    private val httpClient: HttpClient = resolveHttpClientFactory().get(HTTP_CLIENT_PROFILE),
) {
    companion object {
        const val HTTP_CLIENT_PROFILE = "suno-api"
        const val DEFAULT_BASE_URL = "https://api.sunoapi.org/api/v1"
        const val FILE_UPLOAD_BASE_URL = "https://sunoapiorg.redpandaai.co"
        const val TOKEN_DASHBOARD_URL = "https://sunoapi.org/zh-CN/dashboard"
        const val MISSING_API_TOKEN_MESSAGE =
            "还没配置 Suno API Token。没申请过的话请先去 $TOKEN_DASHBOARD_URL 申请 Token，再回设置页填写。"

        private val responseJson = Json {
            ignoreUnknownKeys = true
        }
    }

    private val normalizedBaseUrl = baseUrl.ifBlank { DEFAULT_BASE_URL }

    private fun debug(message: String) {
        println("[SunoApiClient] $message")
    }

    private val api = buildSunoApi(
        baseUrl = normalizedBaseUrl.trimEnd('/') + "/",
        httpClient = httpClient,
    )

    private val auth get() = "Bearer $apiToken"

    private suspend inline fun <T> withAuth(
        crossinline block: suspend (String) -> T,
    ): T {
        if (apiToken.isBlank()) {
            throw IllegalStateException(MISSING_API_TOKEN_MESSAGE)
        }
        return block(auth)
    }

    private suspend fun <T> retryTransientRequest(
        actionLabel: String,
        maxAttempts: Int = 3,
        block: suspend () -> T,
    ): T {
        var attempt = 1
        while (true) {
            try {
                return block()
            } catch (error: Throwable) {
                if (!error.isTransientRequestError() || attempt >= maxAttempts) {
                    throw error
                }
                debug(
                    "$actionLabel transient failure attempt=$attempt/$maxAttempts: ${error::class.simpleName}: ${error.message}"
                )
                delay((attempt * 1_500L).coerceAtMost(4_500L))
                attempt += 1
            }
        }
    }

    // ── 音乐生成 ─────────────────────────────────────────────

    suspend fun generateMusic(request: SunoGenerateRequest): String =
        withAuth { auth ->
            debug("generateMusic submit baseUrl=$normalizedBaseUrl request=$request")
            try {
                api.generateMusic(request, auth).getOrThrow().taskId.also { taskId ->
                    debug("generateMusic accepted taskId=$taskId")
                }
            } catch (error: Throwable) {
                debug("generateMusic failed: ${error::class.simpleName}: ${error.message}")
                throw error
            }
        }

    suspend fun extendMusic(request: SunoExtendRequest): String =
        withAuth { auth ->
            debug("extendMusic submit baseUrl=$normalizedBaseUrl request=$request")
            try {
                api.extendMusic(request, auth).getOrThrow().taskId.also { taskId ->
                    debug("extendMusic accepted taskId=$taskId")
                }
            } catch (error: Throwable) {
                debug("extendMusic failed: ${error::class.simpleName}: ${error.message}")
                throw error
            }
        }

    suspend fun uploadCover(request: SunoUploadCoverRequest): String =
        withAuth { auth ->
            debug("uploadCover submit baseUrl=$normalizedBaseUrl request=$request")
            try {
                api.uploadCover(request, auth).getOrThrow().taskId.also { taskId ->
                    debug("uploadCover accepted taskId=$taskId")
                }
            } catch (error: Throwable) {
                debug("uploadCover failed: ${error::class.simpleName}: ${error.message}")
                throw error
            }
        }

    suspend fun uploadRemoteFile(
        fileUrl: String,
        uploadPath: String = "audio",
        fileName: String? = null,
    ): SunoUploadedFileData =
        withAuth { auth ->
            val normalizedFileUrl = fileUrl.trim()
            val resolvedFileName = fileName ?: buildUploadFileName(normalizedFileUrl)
            val request = SunoFileUrlUploadRequest(
                fileUrl = normalizedFileUrl,
                uploadPath = uploadPath,
                fileName = resolvedFileName,
            )
            debug("uploadRemoteFile submit baseUrl=$FILE_UPLOAD_BASE_URL request=$request")
            try {
                val uploaded = retryTransientRequest("uploadRemoteFile") {
                    val responseText = httpClient.post(FILE_UPLOAD_BASE_URL.trimEnd('/') + "/api/file-url-upload") {
                        header(HttpHeaders.Authorization, auth)
                        setBody(request)
                    }.bodyAsText()
                    responseJson
                        .decodeFromString<ApiResult<SunoUploadedFileData>>(responseText)
                        .getOrThrow()
                }
                debug("uploadRemoteFile success sourceUrl=$normalizedFileUrl fileUrl=${uploaded.resolvedUrl}")
                uploaded
            } catch (error: Throwable) {
                debug("uploadRemoteFile failed: ${error::class.simpleName}: ${error.message}")
                throw error
            }
        }

    suspend fun uploadLocalFile(
        bytes: ByteArray,
        fileName: String,
        contentType: String? = null,
        uploadPath: String = "audio",
    ): SunoUploadedFileData =
        withAuth { auth ->
            if (bytes.isEmpty()) {
                throw IllegalArgumentException("选择的本地文件为空，无法上传")
            }
            val resolvedFileName = sanitizeUploadFileName(
                rawName = fileName,
                fallbackPrefix = "local-audio",
                fallbackExtension = contentType
                    ?.substringAfter('/', "")
                    ?.substringBefore(';')
                    ?.trim()
                    ?.ifBlank { null },
            )
            val resolvedContentType = contentType
                ?.trim()
                ?.ifBlank { null }
                ?: guessContentTypeFromFileName(resolvedFileName)
            debug(
                "uploadLocalFile submit baseUrl=$FILE_UPLOAD_BASE_URL fileName=$resolvedFileName size=${bytes.size} contentType=$resolvedContentType"
            )
            try {
                val uploaded = retryTransientRequest("uploadLocalFile") {
                    val boundary = buildMultipartBoundary()
                    val requestBody = buildLocalFileUploadBody(
                        boundary = boundary,
                        uploadPath = uploadPath,
                        fileName = resolvedFileName,
                        contentType = resolvedContentType,
                        bytes = bytes,
                    )
                    val multipartContent = object : OutgoingContent.ByteArrayContent() {
                        override val contentLength: Long = requestBody.size.toLong()
                        override val contentType: ContentType =
                            ContentType.MultiPart.FormData.withParameter("boundary", boundary)

                        override fun bytes(): ByteArray = requestBody
                    }
                    val responseText = httpClient.post(FILE_UPLOAD_BASE_URL.trimEnd('/') + "/api/file-stream-upload") {
                        header(HttpHeaders.Authorization, auth)
                        setBody(multipartContent)
                    }.bodyAsText()
                    val result = responseJson.decodeFromString<ApiResult<SunoUploadedFileData>>(responseText)
                    if (!result.isSuccess || result.data == null) {
                        debug("uploadLocalFile response error: $responseText")
                    }
                    result.getOrThrow()
                }
                debug("uploadLocalFile success fileName=$resolvedFileName fileUrl=${uploaded.resolvedUrl}")
                uploaded
            } catch (error: Throwable) {
                debug("uploadLocalFile failed: ${error::class.simpleName}: ${error.message}")
                throw error
            }
        }

    suspend fun uploadExtend(request: SunoUploadExtendRequest): String =
        withAuth { auth ->
            api.uploadExtend(request, auth).getOrThrow().taskId
        }

    suspend fun addVocals(request: SunoAddVocalsRequest): String =
        withAuth { auth ->
            api.addVocals(request, auth).getOrThrow().taskId
        }

    suspend fun addInstrumental(request: SunoAddInstrumentalRequest): String =
        withAuth { auth ->
            api.addInstrumental(request, auth).getOrThrow().taskId
        }

    suspend fun generateMusicCover(request: SunoMusicCoverRequest): String =
        withAuth { auth ->
            debug("generateMusicCover submit baseUrl=$normalizedBaseUrl request=$request")
            try {
                api.generateMusicCover(request, auth).getOrThrow().taskId.also { taskId ->
                    debug("generateMusicCover accepted taskId=$taskId")
                }
            } catch (error: Throwable) {
                debug("generateMusicCover failed: ${error::class.simpleName}: ${error.message}")
                throw error
            }
        }

    suspend fun replaceSection(request: SunoReplaceSectionRequest): String =
        withAuth { auth ->
            debug("replaceSection submit baseUrl=$normalizedBaseUrl request=$request")
            try {
                api.replaceSection(request, auth).getOrThrow().taskId.also { taskId ->
                    debug("replaceSection accepted taskId=$taskId")
                }
            } catch (error: Throwable) {
                debug("replaceSection failed: ${error::class.simpleName}: ${error.message}")
                throw error
            }
        }

    // ── 查询 ─────────────────────────────────────────────────

    suspend fun getTaskDetail(taskId: String): SunoTaskDetail? =
        withAuth { auth ->
            try {
                retryTransientRequest("getTaskDetail taskId=$taskId") {
                    api.getTaskDetail(taskId, auth).getOrNull()
                }.also { detail ->
                    debug(
                        "getTaskDetail taskId=$taskId status=${detail?.status} error=${detail?.errorMessage ?: detail?.errorCode}"
                    )
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Throwable) {
                debug("getTaskDetail failed taskId=$taskId: ${error::class.simpleName}: ${error.message}")
                throw error
            }
        }

    suspend fun getCoverDetail(taskId: String): SunoTaskDetail? =
        withAuth { auth ->
            retryTransientRequest("getCoverDetail taskId=$taskId") {
                api.getCoverDetail(taskId, auth).getOrNull()
            }
        }

    // ── 歌词 ─────────────────────────────────────────────────

    suspend fun generateLyrics(request: SunoLyricsRequest): String =
        withAuth { auth ->
            api.generateLyrics(request, auth).getOrThrow().taskId
        }

    suspend fun getLyricsDetail(taskId: String): SunoLyricsTaskDetail? =
        withAuth { auth ->
            retryTransientRequest("getLyricsDetail taskId=$taskId") {
                api.getLyricsDetail(taskId, auth).getOrNull()
            }
        }

    suspend fun getTimestampedLyrics(request: SunoTimestampedLyricsRequest): SunoTimestampedLyricsData =
        withAuth { auth ->
            retryTransientRequest("getTimestampedLyrics taskId=${request.taskId}") {
                api.getTimestampedLyrics(request, auth).getOrThrow()
            }
        }

    // ── Persona ──────────────────────────────────────────────

    suspend fun generatePersona(request: SunoGeneratePersonaRequest): SunoPersonaData =
        withAuth { auth ->
            debug("generatePersona submit baseUrl=$normalizedBaseUrl request=$request")
            try {
                api.generatePersona(request, auth).getOrThrow().also { data ->
                    debug("generatePersona accepted personaId=${data.personaId}")
                }
            } catch (error: Throwable) {
                debug("generatePersona failed: ${error::class.simpleName}: ${error.message}")
                throw error
            }
        }

    // ── 音频处理 ─────────────────────────────────────────────

    suspend fun vocalRemoval(request: SunoVocalRemovalRequest): String =
        withAuth { auth ->
            api.vocalRemoval(request, auth).getOrThrow().taskId
        }

    suspend fun boostMusicStyle(request: SunoBoostStyleRequest): SunoBoostStyleData =
        withAuth { auth ->
            api.boostMusicStyle(request, auth).getOrThrow()
        }

    suspend fun convertToWav(request: SunoWavRequest): String =
        withAuth { auth ->
            api.convertToWav(request, auth).getOrThrow().taskId
        }

    // ── 账户 ─────────────────────────────────────────────────

    suspend fun getCredits(): Int =
        withAuth { auth ->
            debug("getCredits request baseUrl=$normalizedBaseUrl")
            val creditsValue = retryTransientRequest("getCredits") {
                val responseText = httpClient.get(normalizedBaseUrl.trimEnd('/') + "/generate/credit") {
                    header(HttpHeaders.Authorization, auth)
                }.bodyAsText()

                responseJson.parseToJsonElement(responseText)
                    .jsonObject["data"]
                    ?.jsonPrimitive
                    ?.doubleOrNull
                    ?: throw RuntimeException("Suno 积分响应格式不正确")
            }

            creditsValue.roundToInt().also { credits ->
                debug("getCredits success credits=$credits")
            }
        }

    // ── 轮询 ─────────────────────────────────────────────────

    /** 轮询等待音乐生成任务完成 */
    suspend fun waitForCompletion(
        taskId: String,
        maxWaitMs: Long = 600_000L,
        pollIntervalMs: Long = 30_000L,
        onStatusUpdate: ((SunoTaskDetail?) -> Unit)? = null,
    ): SunoTaskDetail {
        val startTime = getTimeMillis()
        while (getTimeMillis() - startTime < maxWaitMs) {
            val detail = try {
                getTaskDetail(taskId)
            } catch (error: Throwable) {
                if (error.isTransientPollingError()) {
                    debug(
                        "waitForCompletion transient poll failure taskId=$taskId: ${error::class.simpleName}: ${error.message}"
                    )
                    delay(3_000L)
                    continue
                }
                throw error
            }
            onStatusUpdate?.invoke(detail)
            when {
                detail?.isSuccess == true -> return detail
                detail?.isFailed == true -> throw RuntimeException(
                    "任务失败: ${detail.failureReason()}"
                )

                else -> delay(pollIntervalMs)
            }
        }
        throw RuntimeException("任务超时，已等待 ${maxWaitMs / 1000} 秒")
    }

    private fun Throwable.isTransientPollingError(): Boolean {
        return isTransientRequestError()
    }

    private fun Throwable.isTransientRequestError(): Boolean {
        val typeName = this::class.simpleName.orEmpty()
        val text = message.orEmpty()
        return typeName == "EOFException" ||
            typeName.contains("Timeout", ignoreCase = true) ||
            text.contains("Not enough data available", ignoreCase = true) ||
            text.contains("unexpected end", ignoreCase = true) ||
            text.contains("connection reset", ignoreCase = true) ||
            text.contains("broken pipe", ignoreCase = true)
    }

    private fun SunoTaskDetail.failureReason(): String {
        val rawMessage = errorMessage?.trim().orEmpty()
        return when {
            rawMessage.contains("matches existing work of art", ignoreCase = true) -> {
                "上传音频命中作品保护，Suno 拒绝翻唱。请换一条你有权使用、且不是现成正式发行版本的音频再试。"
            }

            rawMessage.isNotBlank() -> rawMessage
            errorCode != null -> "错误码 $errorCode"
            !status.isNullOrBlank() -> status
            else -> "未知错误"
        }
    }

    private fun buildUploadFileName(fileUrl: String): String {
        val cleanUrl = fileUrl.substringBefore('?').substringBefore('#')
        val rawName = cleanUrl.substringAfterLast('/').trim()
        if (rawName.isNotBlank() && rawName.contains('.')) {
            return rawName
        }
        val extension = cleanUrl.substringAfterLast('.', "").trim().lowercase()
            .takeIf { it.isNotBlank() && it.length <= 8 && it.all(Char::isLetterOrDigit) }
        val stamp = getTimeMillis()
        return if (extension != null) {
            "remote-audio-$stamp.$extension"
        } else {
            "remote-audio-$stamp.mp3"
        }
    }

    private fun sanitizeUploadFileName(
        rawName: String?,
        fallbackPrefix: String,
        fallbackExtension: String? = null,
    ): String {
        val normalizedName = rawName
            ?.trim()
            ?.replace(Regex("[^A-Za-z0-9._-]"), "-")
            ?.trim('.', '-', '_')
            .orEmpty()
        if (normalizedName.isNotBlank() && normalizedName.contains('.')) {
            return normalizedName
        }
        val cleanExtension = fallbackExtension
            ?.trim()
            ?.lowercase()
            ?.trim('.')
            ?.takeIf { it.isNotBlank() && it.length <= 8 && it.all(Char::isLetterOrDigit) }
            ?: "mp3"
        val stem = normalizedName.ifBlank { "$fallbackPrefix-${getTimeMillis()}" }
        return "$stem.$cleanExtension"
    }

    private fun guessContentTypeFromFileName(fileName: String): String {
        return when (fileName.substringAfterLast('.', "").lowercase()) {
            "mp3" -> "audio/mpeg"
            "wav" -> "audio/wav"
            "m4a", "mp4" -> "audio/mp4"
            "aac" -> "audio/aac"
            "flac" -> "audio/flac"
            "ogg", "oga" -> "audio/ogg"
            "opus" -> "audio/opus"
            else -> ContentType.Application.OctetStream.toString()
        }
    }

    private fun buildMultipartBoundary(): String {
        return "----VibePocketSuno${getTimeMillis()}"
    }

    private fun buildLocalFileUploadBody(
        boundary: String,
        uploadPath: String,
        fileName: String,
        contentType: String,
        bytes: ByteArray,
    ): ByteArray {
        val lineBreak = "\r\n"
        val chunks = buildList {
            add("--$boundary$lineBreak".encodeToByteArray())
            add("Content-Disposition: form-data; name=\"uploadPath\"$lineBreak$lineBreak".encodeToByteArray())
            add(uploadPath.encodeToByteArray())
            add(lineBreak.encodeToByteArray())

            add("--$boundary$lineBreak".encodeToByteArray())
            add("Content-Disposition: form-data; name=\"fileName\"$lineBreak$lineBreak".encodeToByteArray())
            add(fileName.encodeToByteArray())
            add(lineBreak.encodeToByteArray())

            add("--$boundary$lineBreak".encodeToByteArray())
            add("Content-Disposition: form-data; name=\"file\"; filename=\"$fileName\"$lineBreak".encodeToByteArray())
            add("Content-Type: $contentType$lineBreak$lineBreak".encodeToByteArray())
            add(bytes)
            add(lineBreak.encodeToByteArray())
            add("--$boundary--$lineBreak".encodeToByteArray())
        }
        val totalSize = chunks.sumOf { it.size }
        val payload = ByteArray(totalSize)
        var offset = 0
        chunks.forEach { chunk ->
            chunk.copyInto(payload, destinationOffset = offset)
            offset += chunk.size
        }
        return payload
    }
}

private fun resolveHttpClientFactory(): HttpClientFactory {
    return HttpClientFactory.shared()
}
