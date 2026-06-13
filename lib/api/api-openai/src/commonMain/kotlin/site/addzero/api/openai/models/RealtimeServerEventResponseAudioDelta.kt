// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Returned when the model-generated audio is updated.
 */
@Serializable
data class RealtimeServerEventResponseAudioDelta(
    /**
     * The unique ID of the server event.
     */
    @SerialName("event_id")
    val eventId: String,
    /**
     * The event type, must be `response.output_audio.delta`.
     */
    val type: String,
    /**
     * The ID of the response.
     */
    @SerialName("response_id")
    val responseId: String,
    /**
     * The ID of the item.
     */
    @SerialName("item_id")
    val itemId: String,
    /**
     * The index of the output item in the response.
     */
    @SerialName("output_index")
    val outputIndex: Int,
    /**
     * The index of the content part in the item's content array.
     */
    @SerialName("content_index")
    val contentIndex: Int,
    /**
     * Base64-encoded audio data delta.
     */
    val delta: String
)
