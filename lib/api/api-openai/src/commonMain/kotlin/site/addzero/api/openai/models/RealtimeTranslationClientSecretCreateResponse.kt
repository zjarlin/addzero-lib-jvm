// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Response from creating a translation session and client secret for the Realtime API.
 */
@Serializable
data class RealtimeTranslationClientSecretCreateResponse(
    /**
     * The generated client secret value.
     */
    val value: String,
    /**
     * Expiration timestamp for the client secret, in seconds since epoch.
     */
    @SerialName("expires_at")
    val expiresAt: Long,
    val session: site.addzero.api.openai.models.RealtimeTranslationSession
)
