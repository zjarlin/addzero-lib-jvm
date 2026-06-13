// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * Response from creating a session and client secret for the Realtime API.
 */
@Serializable
data class RealtimeCreateClientSecretResponse(
    /**
     * The generated client secret value.
     */
    val value: String,
    /**
     * Expiration timestamp for the client secret, in seconds since epoch.
     */
    @SerialName("expires_at")
    val expiresAt: Long,
    /**
     * The session configuration for either a realtime or transcription session.
     */
    val session: JsonElement
)
