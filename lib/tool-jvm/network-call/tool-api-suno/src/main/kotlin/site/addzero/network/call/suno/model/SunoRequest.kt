package site.addzero.network.call.suno.model

import com.alibaba.fastjson2.annotation.JSONField

/**
 * 生成音乐请求（灵感模式）
 */
data class GenerateMusicInspirationRequest(
    @JSONField(name = "gpt_description_prompt")
    val gptDescriptionPrompt: String,
    
    @JSONField(name = "make_instrumental")
    val makeInstrumental: Boolean = false,
    
    @JSONField(name = "mv")
    val mv: String = "chirp-v5",
    
    val prompt: String = ""
)

/**
 * 生成音乐请求（自定义模式）
 */
data class GenerateMusicCustomRequest(
    val prompt: String,
    val mv: String = "chirp-v5",
    val title: String = "",
    val tags: String = "",
    
    @JSONField(name = "continue_at")
    val continueAt: Int? = null,
    
    @JSONField(name = "continue_clip_id")
    val continueClipId: String = "",
    
    val task: String = ""
)

/**
 * 扩展音乐请求（续写模式）
 */
data class ExtendMusicRequest(
    val prompt: String,
    val mv: String = "chirp-v5",
    val title: String = "",
    val tags: String = "",
    
    @JSONField(name = "continue_at")
    val continueAt: Int,
    
    @JSONField(name = "continue_clip_id")
    val continueClipId: String,
    
    val task: String = "extend"
)

/**
 * 生成歌词请求
 */
data class GenerateLyricsRequest(
    val prompt: String
)

/**
 * 批量获取任务请求
 */
data class BatchFetchRequest(
    val ids: List<String>
)

/**
 * 拼接歌曲请求
 */
data class ConcatSongsRequest(
    @JSONField(name = "clip_id")
    val clipId: String
)
