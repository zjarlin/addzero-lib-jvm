package site.addzero.network.call.suno.log

/**
 * Suno API 日志策略接口
 */
interface SunoLogStrategy {
    /**
     * 记录请求和响应
     * @param bizName 业务名称（如 submit_music, fetch_task）
     * @param requestBodyString 请求体 JSON
     * @param responseString 响应体 JSON
     */
    fun log(bizName: String, requestBodyString: String, responseString: String)
}
