// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Emitted at the beginning of a Response to indicate the updated rate limits. When a Response is
 * created some tokens will be "reserved" for the output tokens, the rate limits shown here reflect
 * that reservation, which is then adjusted accordingly once the Response is completed.
 */
@Serializable
data class RealtimeServerEventRateLimitsUpdated(
    /**
     * The unique ID of the server event.
     */
    @SerialName("event_id")
    val eventId: String,
    /**
     * The event type, must be `rate_limits.updated`.
     */
    val type: String,
    /**
     * List of rate limit information.
     */
    @SerialName("rate_limits")
    val rateLimits: List<site.addzero.api.openai.models.RealtimeServerEventRateLimitsUpdatedRateLimit>
)
