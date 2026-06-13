// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Parameters used to decline an incoming SIP call handled by the Realtime API.
 */
@Serializable
data class RealtimeCallRejectRequest(
    /**
     * SIP response code to send back to the caller. Defaults to `603` (Decline) when omitted.
     */
    @SerialName("status_code")
    val statusCode: Int? = null
)
