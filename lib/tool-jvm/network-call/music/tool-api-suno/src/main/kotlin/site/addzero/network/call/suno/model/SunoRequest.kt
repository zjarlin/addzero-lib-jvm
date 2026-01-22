package site.addzero.network.call.suno.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 统一的提交请求密封类
 * 用于封装所有 Suno 音乐生成的请求类型
 */
@Serializable
sealed class SunoSubmitRequest {
    /** 模型版本，如 chirp-v5 */
    abstract val mv: String

    /**
     * 灵感模式 (Inspiration Mode)
     * 通过描述生成音乐，AI 自动创作歌词和旋律
     */
    @Serializable
    @SerialName("inspiration")
    data class Inspiration(
        /** GPT 描述提示词，描述想要的音乐风格和内容 */
        @SerialName("gpt_description_prompt")
        val gptDescriptionPrompt: String,

        /** 是否生成纯音乐（无人声） */
        @SerialName("make_instrumental")
        val makeInstrumental: Boolean = false,

        /** 模型版本，默认 chirp-v5 */
        override val mv: String = "chirp-v5"
    ) : SunoSubmitRequest()

    /**
     * 自定义模式 (Custom Mode)
     * 使用自定义歌词和标签生成音乐
     */
    @Serializable
    @SerialName("custom")
    data class Custom(
        /** 歌词内容 */
        val prompt: String,

        /** 音乐风格标签，如 "pop, rock, chinese" */
        val tags: String = "",

        /** 歌曲标题 */
        val title: String = "",

        /** 是否生成纯音乐（无人声） */
        @SerialName("make_instrumental")
        val makeInstrumental: Boolean = false,

        /** 模型版本，默认 chirp-v5 */
        override val mv: String = "chirp-v5"
    ) : SunoSubmitRequest()

    /**
     * 续写模式 (Extend Mode)
     * 基于现有音频片段继续生成
     */
    @Serializable
    @SerialName("extend")
    data class Extend(
        /** 要续写的音频片段 ID */
        @SerialName("continue_clip_id")
        val continueClipId: String,

        /** 从第几秒开始续写 */
        @SerialName("continue_at")
        val continueAt: Int,

        /** 续写的歌词内容 */
        val prompt: String = "",

        /** 音乐风格标签 */
        val tags: String = "",

        /** 歌曲标题 */
        val title: String = "",

        /** 模型版本，默认 chirp-v5 */
        override val mv: String = "chirp-v5"
    ) : SunoSubmitRequest()

    /**
     * 歌手风格模式 (Artist Consistency / Persona)
     * 使用特定歌手的声音风格生成音乐
     */
    @Serializable
    @SerialName("artist_consistency")
    data class ArtistConsistency(
        /** 歌手角色 ID */
        @SerialName("persona_id")
        val personaId: String,

        /** 歌手音频片段 ID，用于提取声音特征 */
        @SerialName("artist_clip_id")
        val artistClipId: String,

        /** 声音性别，如 "male", "female" */
        @SerialName("vocal_gender")
        val vocalGender: String? = "male",

        /** 生成类型，默认 "TEXT" */
        @SerialName("generation_type")
        val generationType: String = "TEXT",

        /** 负面标签，指定不想要的风格 */
        @SerialName("negative_tags")
        val negativeTags: String? = null,

        /** 任务类型标识 */
        val task: String = "artist_consistency",

        /** 模型版本，默认推荐使用 chirp-v4-tau */
        override val mv: String = "chirp-v4-tau"
    ) : SunoSubmitRequest()

    /**
     * 二次创作/上传模式 (Remix / Upload Extend)
     * 基于上传的音频进行二次创作
     */
    @Serializable
    @SerialName("upload_extend")
    data class Remix(
        /** 上传的音频片段 ID */
        @SerialName("continue_clip_id")
        val continueClipId: String,

        /** 创作提示词/歌词 */
        val prompt: String = "",

        /** 音乐风格标签 */
        val tags: String = "",

        /** 歌曲标题 */
        val title: String = "",

        /** 任务类型标识 */
        val task: String = "upload_extend",

        /** 模型版本，默认 chirp-v5 */
        override val mv: String = "chirp-v5"
    ) : SunoSubmitRequest()

    /**
     * 拼接模式 (Concat Mode)
     * 将多个音频片段拼接成完整歌曲
     */
    @Serializable
    @SerialName("concat")
    data class Concat(
        /** 要拼接的音频片段 ID */
        @SerialName("clip_id")
        val clipId: String,

        /** 是否填充中间部分 */
        @SerialName("is_infill")
        val isInfill: Boolean = false,

        /** 模型版本，默认 chirp-v5 */
        override val mv: String = "chirp-v5"
    ) : SunoSubmitRequest()
}

/**
 * 生成歌词请求
 * 使用 AI 根据描述生成歌词
 */
@Serializable
data class GenerateLyricsRequest(
    /** 歌词描述提示词，描述想要的歌词主题和风格 */
    val prompt: String
)

/**
 * 批量获取任务请求
 * 一次查询多个任务的状态
 */
@Serializable
data class BatchFetchRequest(
    /** 任务 ID 列表 */
    val ids: List<String>
)
