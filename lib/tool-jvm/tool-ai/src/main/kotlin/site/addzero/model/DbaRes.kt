package site.addzero.model

/**
 * DBA响应数据类
 *
 * @property choices 选择列表
 * @property `object` 对象类型
 * @property usage 使用情况
 * @property created 创建时间戳
 * @property systemFingerprint 系统指纹
 * @property model 模型名称
 * @property id ID标识
 */
data class Dba(
    val choices: List<ChoicesDTO?>? = null,
    val `object`: String? = null,
    val usage: UsageDTO? = null,
    val created: Int? = null,
    val systemFingerprint: Any? = null,
    val model: String? = null,
    val id: String? = null,
) {
    /**
     * 使用情况数据类
     *
     * @property promptTokens 提示令牌数
     * @property completionTokens 完成令牌数
     * @property totalTokens 总令牌数
     */
    data class UsageDTO(
        val promptTokens: Int? = null,
        val completionTokens: Int? = null,
        val totalTokens: Int? = null,
    )

    /**
     * 选择项数据类
     *
     * @property message 消息内容
     * @property finishReason 完成原因
     * @property index 索引
     * @property logprobs 日志概率
     */
    data class ChoicesDTO(
        val message: MessageDTO? = null,
        val finishReason: String? = null,
        val index: Int? = null,
        val logprobs: Any? = null,
    ) {
        /**
         * 消息数据类
         *
         * @property role 角色
         * @property content 内容
         */
        data class MessageDTO(
            val role: String? = null,
            val content: String? = null,
        )
    }
}
