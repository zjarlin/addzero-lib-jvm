// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Retain a fraction of the conversation tokens when the conversation exceeds the input token limit.
 * This allows you to amortize truncations across multiple turns, which can help improve cached token
 * usage.
 */
@Serializable
data class RealtimeTruncation2(
    /**
     * Use retention ratio truncation.
     */
    val type: String,
    /**
     * Fraction of post-instruction conversation tokens to retain (`0.0` - `1.0`) when the conversation
     * exceeds the input token limit. Setting this to `0.8` means that messages will be dropped until 80%
     * of the maximum allowed tokens are used. This helps reduce the frequency of truncations and improve
     * cache rates.
     */
    @SerialName("retention_ratio")
    val retentionRatio: Double,
    /**
     * Optional custom token limits for this truncation strategy. If not provided, the model's default
     * token limits will be used.
     */
    @SerialName("token_limits")
    val tokenLimits: site.addzero.api.openai.models.RealtimeTruncation2TokenLimits? = null
)
