// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * Emitted when an annotation is added to output text content.
 */
@Serializable
data class ResponseOutputTextAnnotationAddedEvent(
    /**
     * The type of the event. Always 'response.output_text.annotation.added'.
     */
    val type: String,
    /**
     * The unique identifier of the item to which the annotation is being added.
     */
    @SerialName("item_id")
    val itemId: String,
    /**
     * The index of the output item in the response's output array.
     */
    @SerialName("output_index")
    val outputIndex: Int,
    /**
     * The index of the content part within the output item.
     */
    @SerialName("content_index")
    val contentIndex: Int,
    /**
     * The index of the annotation within the content part.
     */
    @SerialName("annotation_index")
    val annotationIndex: Int,
    /**
     * The sequence number of this event.
     */
    @SerialName("sequence_number")
    val sequenceNumber: Int,
    /**
     * The annotation object being added. (See annotation schema for details.)
     */
    val annotation: Map<String, JsonElement>
)
