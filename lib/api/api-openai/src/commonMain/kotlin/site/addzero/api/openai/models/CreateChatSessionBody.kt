// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Parameters for provisioning a new ChatKit session.
 */
@Serializable
data class CreateChatSessionBody(
    /**
     * Workflow that powers the session.
     */
    val workflow: site.addzero.api.openai.models.WorkflowParam,
    /**
     * A free-form string that identifies your end user; ensures this Session can access other objects that
     * have the same `user` scope.
     */
    val user: String,
    /**
     * Optional override for session expiration timing in seconds from creation. Defaults to 10 minutes.
     */
    @SerialName("expires_after")
    val expiresAfter: site.addzero.api.openai.models.ExpiresAfterParam? = null,
    /**
     * Optional override for per-minute request limits. When omitted, defaults to 10.
     */
    @SerialName("rate_limits")
    val rateLimits: site.addzero.api.openai.models.RateLimitsParam? = null,
    /**
     * Optional overrides for ChatKit runtime configuration features
     */
    @SerialName("chatkit_configuration")
    val chatkitConfiguration: site.addzero.api.openai.models.ChatkitConfigurationParam? = null
)
