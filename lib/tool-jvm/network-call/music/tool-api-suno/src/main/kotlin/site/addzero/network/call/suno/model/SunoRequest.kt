package site.addzero.network.call.suno.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Suno 音乐生成统一请求
 * 整合了所有生成模式的参数
 */
@Serializable
data class SunoMusicRequest(
    // ============ 通用字段 ============
    /** 模型版本，如 chirp-v5, chirp-v4-tau */
    val mv: String = "chirp-v5",

    // ============ 灵感模式 (Inspiration Mode) ============
    /** GPT 描述提示词，描述想要的音乐风格和内容 */
    @SerialName("gpt_description_prompt")
    val gptDescriptionPrompt: String? = null,


  // ============ 自定义模式 (Custom Mode) + 续写模式 (Extend Mode) ============
    /*任务完成后的回调通知地址*/
    @SerialName("notify_hook")
    val notifyHook : String? = null,


    // ============ 自定义模式 (Custom Mode) + 续写模式 (Extend Mode) ============
    /** 歌曲标题 仅用于自定义模式*/
    val title: String? = null,

    /** 音乐风格标签，如 "pop, rock, chinese" */
    val tags: String? = null,

    /** 歌词内容/提示词 */
    val prompt: String,

    /** 是否生成纯音乐（无人声） */
    @SerialName("make_instrumental")
    val makeInstrumental: Boolean? = false,

    // ============ 续写模式 (Extend Mode) + 二次创作模式 (Remix) ============

    /** 任务ID，用于对已有任务进行操作（如续写），可以为 null*/
    @SerialName("task_id")
    val taskId: String? = null,


    /** 要续写/二次创作的音频片段 ID 非必传参数，可以为 null*/
    @SerialName("continue_clip_id")
    val continueClipId: String? = null,

    /** 从第几秒开始续写 非必传参数，可以为 null*/
    @SerialName("continue_at")
    val continueAt: Int? = null,

    // ============ 歌手风格模式 (Artist Consistency) ============
    /** 歌手角色 ID */
    @SerialName("persona_id")
    val personaId: String? = null,

    /** 歌手音频片段 ID，用于提取声音特征 */
    @SerialName("artist_clip_id")
    val artistClipId: String? = null,

    /** 声音性别男女，如 "m", "f" */

    @SerialName("vocal_gender")
    val vocalGender: String? = "m",

    /** 生成类型，默认 "TEXT" */
    @SerialName("generation_type")
    val generationType: String? = null,

    /** 负面标签，指定不想要的风格 */
    @SerialName("negative_tags")
    val negativeTags: String? = null,

    // ============ 拼接模式 (Concat Mode) ============
    /** 要拼接的音频片段 ID */
    @SerialName("clip_id")
    val clipId: String? = null,

    /** 是否填充中间部分 */
    @SerialName("is_infill")
    val isInfill: Boolean? = null,

    // ============ 任务类型标识 ============
    /** 任务类型：artist_consistency, upload_extend, extend 等 */
    val task: String? = "extend"
)

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

/**
 * 拼接歌曲请求
 */
@Serializable
data class ConcatSongsRequest(
    /** 音频片段 ID */
    @SerialName("clip_id")
    val clipId: String
)
