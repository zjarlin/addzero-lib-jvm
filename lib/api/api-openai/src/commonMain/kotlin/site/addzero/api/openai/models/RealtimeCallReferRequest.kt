// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Parameters required to transfer a SIP call to a new destination using the Realtime API.
 */
@Serializable
data class RealtimeCallReferRequest(
    /**
     * URI that should appear in the SIP Refer-To header. Supports values like `tel:+14155550123` or
     * `sip:agent@example.com`.
     */
    @SerialName("target_uri")
    val targetUri: String
)
