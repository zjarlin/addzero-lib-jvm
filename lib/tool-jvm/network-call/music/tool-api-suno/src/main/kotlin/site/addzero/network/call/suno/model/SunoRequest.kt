package site.addzero.network.call.suno.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 统一的提交请求密封类
 */
@Serializable
sealed class SunoSubmitRequest {
    abstract val mv: String

    /**
     * 灵感模式 (Inspiration Mode)
     */
    @Serializable
    @SerialName("inspiration")
    data class Inspiration(
        @SerialName("gpt_description_prompt")
        val gptDescriptionPrompt: String,
        @SerialName("make_instrumental")
        val makeInstrumental: Boolean = false,
        override val mv: String = "chirp-v5"
    ) : SunoSubmitRequest()

    /**
     * 自定义模式 (Custom Mode)
     */
    @Serializable
    @SerialName("custom")
    data class Custom(
        val prompt: String,
        val tags: String = "",
        val title: String = "",
        @SerialName("make_instrumental")
        val makeInstrumental: Boolean = false,
        override val mv: String = "chirp-v5"
    ) : SunoSubmitRequest()

    /**
     * 续写模式 (Extend Mode)
     */
    @Serializable
    @SerialName("extend")
    data class Extend(
        @SerialName("continue_clip_id")
        val continueClipId: String,
        @SerialName("continue_at")
        val continueAt: Int,
        val prompt: String = "",
        val tags: String = "",
        val title: String = "",
        override val mv: String = "chirp-v5"
    ) : SunoSubmitRequest()

    /**
     * 歌手风格模式 (Artist Consistency / Persona)
     */
    @Serializable
    @SerialName("artist_consistency")
    data class ArtistConsistency(
        @SerialName("persona_id")
        val personaId: String,
        @SerialName("artist_clip_id")
        val artistClipId: String,
        @SerialName("vocal_gender")
        val vocalGender: String? = null,
        @SerialName("generation_type")
        val generationType: String = "TEXT",
        @SerialName("negative_tags")
        val negativeTags: String? = null,
        val task: String = "artist_consistency",
        override val mv: String = "chirp-v4-tau" // 默认推荐使用 tau 版本
    ) : SunoSubmitRequest()

    /**
     * 二次创作/上传模式 (Remix / Upload Extend)
     */
    @Serializable
    @SerialName("upload_extend")
    data class Remix(
        @SerialName("continue_clip_id")
        val continueClipId: String,
        val prompt: String = "",
        val tags: String = "",
        val title: String = "",
        val task: String = "upload_extend",
        override val mv: String = "chirp-v5"
    ) : SunoSubmitRequest()

    /**
     * 拼接模式 (Concat Mode)
     */
    @Serializable
    @SerialName("concat")
    data class Concat(
        @SerialName("clip_id")
        val clipId: String,
        @SerialName("is_infill")
        val isInfill: Boolean = false,
        override val mv: String = "chirp-v5"
    ) : SunoSubmitRequest()
}

/**
 * 生成歌词请求
 */
@Serializable
data class GenerateLyricsRequest(
    val prompt: String
)

/**
 * 批量获取任务请求
 */
@Serializable
data class BatchFetchRequest(
    val ids: List<String>
)
