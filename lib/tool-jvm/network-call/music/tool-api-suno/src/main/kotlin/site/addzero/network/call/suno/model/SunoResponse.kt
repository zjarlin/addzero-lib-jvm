package site.addzero.network.call.suno.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * VectorEngine API 统一响应
 */
@Serializable
data class VectorEngineResponse<T>(
    val code: String,
    val message: String? = null,
    val data: T? = null
)

/**
 * Suno 任务信息
 */
@Serializable
data class SunoTask(
    val id: String? = null,
    val status: String? = null,
    val prompt: String? = null,
    @SerialName("gpt_description_prompt")
    val gptDescriptionPrompt: String? = null,
    val title: String? = null,
    val tags: String? = null,
    val mv: String? = null,
    val type: String? = null,
    val duration: Double? = null,
    @SerialName("audio_url")
    val audioUrl: String? = null,
    @SerialName("video_url")
    val videoUrl: String? = null,
    @SerialName("created_at")
    val createdAt: String? = null,
    @SerialName("error_message")
    val errorMessage: String? = null,
    @SerialName("clip_id")
    val clipId: String? = null,
    @SerialName("instrumental")
    val instrumental: Boolean? = null
)
