package site.addzero.network.call.suno.model

import com.alibaba.fastjson2.annotation.JSONField

/**
 * VectorEngine API 响应包装
 */
data class VectorEngineResponse<T>(
    val code: String,
    val data: T?,
    val message: String
)

/**
 * Suno 任务信息
 */
data class SunoTask(
    val id: String,
    val title: String?,
    
    @JSONField(name = "image_url")
    val imageUrl: String?,
    
    @JSONField(name = "lyric")
    val lyric: String?,
    
    @JSONField(name = "audio_url")
    val audioUrl: String?,
    
    @JSONField(name = "video_url")
    val videoUrl: String?,
    
    @JSONField(name = "created_at")
    val createdAt: String?,
    
    val model: String?,
    val prompt: String?,
    val tags: String?,
    val duration: Double?,
    val status: String?,
    val error: String?
)
