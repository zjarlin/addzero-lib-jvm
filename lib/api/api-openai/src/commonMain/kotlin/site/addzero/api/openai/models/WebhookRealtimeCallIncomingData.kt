// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Event data payload.
 */
@Serializable
data class WebhookRealtimeCallIncomingData(
    /**
     * The unique ID of this call.
     */
    @SerialName("call_id")
    val callId: String,
    /**
     * Headers from the SIP Invite.
     */
    @SerialName("sip_headers")
    val sipHeaders: List<site.addzero.api.openai.models.WebhookRealtimeCallIncomingDataSipHeader>
)
