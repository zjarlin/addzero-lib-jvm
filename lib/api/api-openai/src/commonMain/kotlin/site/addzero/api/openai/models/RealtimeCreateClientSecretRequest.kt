// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * Create a session and client secret for the Realtime API. The request can specify either a realtime
 * or a transcription session configuration. [Learn more about the Realtime
 * API](/docs/guides/realtime).
 */
@Serializable
data class RealtimeCreateClientSecretRequest(
    /**
     * Configuration for the client secret expiration. Expiration refers to the time after which a client
     * secret will no longer be valid for creating sessions. The session itself may continue after that
     * time once started. A secret can be used to create multiple sessions until it expires.
     */
    @SerialName("expires_after")
    val expiresAfter: site.addzero.api.openai.models.RealtimeCreateClientSecretRequestExpiresAfter? = null,
    /**
     * Session configuration to use for the client secret. Choose either a realtime session or a
     * transcription session.
     */
    val session: JsonElement? = null
)
