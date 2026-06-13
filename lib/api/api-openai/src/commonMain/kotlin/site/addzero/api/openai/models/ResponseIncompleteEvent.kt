// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * An event that is emitted when a response finishes as incomplete.
 */
@Serializable
data class ResponseIncompleteEvent(
    /**
     * The type of the event. Always `response.incomplete`.
     */
    val type: String,
    /**
     * The response that was incomplete.
     */
    val response: site.addzero.api.openai.models.Response,
    /**
     * The sequence number of this event.
     */
    @SerialName("sequence_number")
    val sequenceNumber: Int
)
