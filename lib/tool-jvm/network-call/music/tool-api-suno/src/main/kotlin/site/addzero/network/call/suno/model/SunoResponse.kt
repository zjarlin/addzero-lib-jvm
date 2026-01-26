package site.addzero.network.call.suno.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Suno 任务信息
 * 包含音乐生成任务的完整状态和结果
 */
@Serializable
data class SunoTask(
    /** 任务唯一标识 ID */
    val id: String? = null,

    /** 任务状态：queued(排队中), processing(处理中), complete(完成), streaming(流式完成), error(失败) */
    val status: String? = null,

    /** 歌词内容/提示词 */
    val prompt: String? = null,

    /** GPT 描述提示词（灵感模式使用） */
    @SerialName("gpt_description_prompt")
    val gptDescriptionPrompt: String? = null,

    /** 歌曲标题 */
    val title: String? = null,

    /** 音乐风格标签 */
    val tags: String? = null,

    /** 使用的模型版本 */
    val mv: String? = null,

    /** 任务类型 */
    val type: String? = null,

    /** 音频时长（秒） */
    val duration: Double? = null,

    /** 音频文件 URL */
    @SerialName("audio_url")
    val audioUrl: String? = null,

    /** 视频文件 URL（带可视化效果） */
    @SerialName("video_url")
    val videoUrl: String? = null,

    /** 任务创建时间 */
    @SerialName("created_at")
    val createdAt: String? = null,

    /** 错误信息（任务失败时） */
    @SerialName("error_message")
    val errorMessage: String? = null,

    /** 错误信息（兼容字段） */
    val error: String? = null,

    /** 音频片段 ID（用于续写或拼接） */
    @SerialName("clip_id")
    val clipId: String? = null,

    /** 是否为纯音乐（无人声） */
    @SerialName("instrumental")
    val instrumental: Boolean? = null
)
