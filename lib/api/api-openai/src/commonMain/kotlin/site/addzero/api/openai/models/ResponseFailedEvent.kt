// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * An event that is emitted when a response fails.
 */
@Serializable
data class ResponseFailedEvent(
    /**
     * The type of the event. Always `response.failed`.
     */
    val type: String,
    /**
     * The sequence number of this event.
     */
    @SerialName("sequence_number")
    val sequenceNumber: Int,
    /**
     * The response that failed.
     */
    val response: site.addzero.api.openai.models.Response
)
