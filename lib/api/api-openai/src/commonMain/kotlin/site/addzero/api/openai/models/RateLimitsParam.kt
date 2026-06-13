// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Controls request rate limits for the session.
 */
@Serializable
data class RateLimitsParam(
    /**
     * Maximum number of requests allowed per minute for the session. Defaults to 10.
     */
    @SerialName("max_requests_per_1_minute")
    val maxRequestsPer1Minute: Int? = null
)
