// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * A header from the SIP Invite.
 */
@Serializable
data class WebhookRealtimeCallIncomingDataSipHeader(
    /**
     * Name of the SIP Header.
     */
    val name: String,
    /**
     * Value of the SIP Header.
     */
    val value: String
)
