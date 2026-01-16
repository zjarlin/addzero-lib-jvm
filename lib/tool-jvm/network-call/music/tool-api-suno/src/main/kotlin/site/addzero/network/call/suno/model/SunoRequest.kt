package site.addzero.network.call.suno.model

import com.alibaba.fastjson2.annotation.JSONField

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

