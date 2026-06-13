// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents a ChatKit session and its resolved configuration.
 */
@Serializable
data class ChatSessionResource(
    /**
     * Identifier for the ChatKit session.
     */
    val id: String,
    /**
     * Type discriminator that is always `chatkit.session`.
     */
    @SerialName("object")
    val objectType: String = "chatkit.session",
    /**
     * Unix timestamp (in seconds) for when the session expires.
     */
    @SerialName("expires_at")
    val expiresAt: Long,
    /**
     * Ephemeral client secret that authenticates session requests.
     */
    @SerialName("client_secret")
    val clientSecret: String,
    /**
     * Workflow metadata for the session.
     */
    val workflow: site.addzero.api.openai.models.ChatkitWorkflow,
    /**
     * User identifier associated with the session.
     */
    val user: String,
    /**
     * Resolved rate limit values.
     */
    @SerialName("rate_limits")
    val rateLimits: site.addzero.api.openai.models.ChatSessionRateLimits,
    /**
     * Convenience copy of the per-minute request limit.
     */
    @SerialName("max_requests_per_1_minute")
    val maxRequestsPer1Minute: Int,
    /**
     * Current lifecycle state of the session.
     */
    val status: site.addzero.api.openai.models.ChatSessionStatus,
    /**
     * Resolved ChatKit feature configuration for the session.
     */
    @SerialName("chatkit_configuration")
    val chatkitConfiguration: site.addzero.api.openai.models.ChatSessionChatkitConfiguration
)
