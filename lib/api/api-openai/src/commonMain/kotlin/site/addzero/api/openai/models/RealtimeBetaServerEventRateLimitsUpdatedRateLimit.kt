// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RealtimeBetaServerEventRateLimitsUpdatedRateLimit(
    /**
     * The name of the rate limit (`requests`, `tokens`).
     */
    val name: String? = null,
    /**
     * The maximum allowed value for the rate limit.
     */
    val limit: Int? = null,
    /**
     * The remaining value before the limit is reached.
     */
    val remaining: Int? = null,
    /**
     * Seconds until the rate limit resets.
     */
    @SerialName("reset_seconds")
    val resetSeconds: Double? = null
)
