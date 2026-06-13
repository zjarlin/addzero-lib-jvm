// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Emitted when a new content part is added.
 */
@Serializable
data class ResponseContentPartAddedEvent(
    /**
     * The type of the event. Always `response.content_part.added`.
     */
    val type: String,
    /**
     * The ID of the output item that the content part was added to.
     */
    @SerialName("item_id")
    val itemId: String,
    /**
     * The index of the output item that the content part was added to.
     */
    @SerialName("output_index")
    val outputIndex: Int,
    /**
     * The index of the content part that was added.
     */
    @SerialName("content_index")
    val contentIndex: Int,
    /**
     * The content part that was added.
     */
    val part: site.addzero.api.openai.models.OutputContent,
    /**
     * The sequence number of this event.
     */
    @SerialName("sequence_number")
    val sequenceNumber: Int
)
