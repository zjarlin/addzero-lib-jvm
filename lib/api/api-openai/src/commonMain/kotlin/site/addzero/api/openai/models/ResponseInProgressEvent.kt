// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Emitted when the response is in progress.
 */
@Serializable
data class ResponseInProgressEvent(
    /**
     * The type of the event. Always `response.in_progress`.
     */
    val type: String,
    /**
     * The response that is in progress.
     */
    val response: site.addzero.api.openai.models.Response,
    /**
     * The sequence number of this event.
     */
    @SerialName("sequence_number")
    val sequenceNumber: Int
)
