// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Active per-minute request limit for the session.
 */
@Serializable
data class ChatSessionRateLimits(
    /**
     * Maximum allowed requests per one-minute window.
     */
    @SerialName("max_requests_per_1_minute")
    val maxRequestsPer1Minute: Int
)
