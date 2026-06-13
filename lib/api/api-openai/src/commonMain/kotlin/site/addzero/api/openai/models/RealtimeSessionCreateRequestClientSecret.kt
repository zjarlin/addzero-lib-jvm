// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Ephemeral key returned by the API.
 */
@Serializable
data class RealtimeSessionCreateRequestClientSecret(
    /**
     * Ephemeral key usable in client environments to authenticate connections to the Realtime API. Use
     * this in client-side environments rather than a standard API token, which should only be used server-
     * side.
     */
    val value: String,
    /**
     * Timestamp for when the token expires. Currently, all tokens expire after one minute.
     */
    @SerialName("expires_at")
    val expiresAt: Long
)
