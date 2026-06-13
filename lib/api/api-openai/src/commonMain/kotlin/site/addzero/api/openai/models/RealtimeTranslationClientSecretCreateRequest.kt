// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Create a translation session and client secret for the Realtime API.
 */
@Serializable
data class RealtimeTranslationClientSecretCreateRequest(
    /**
     * Configuration for the client secret expiration. Expiration refers to the time after which a client
     * secret will no longer be valid for creating sessions. The session itself may continue after that
     * time once started. A secret can be used to create multiple sessions until it expires.
     */
    @SerialName("expires_after")
    val expiresAfter: site.addzero.api.openai.models.RealtimeTranslationClientSecretCreateRequestExpiresAfter? = null,
    val session: site.addzero.api.openai.models.RealtimeTranslationSessionCreateRequest
)
