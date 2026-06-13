// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Returned when an input audio buffer is committed, either by the client or automatically in server
 * VAD mode. The `item_id` property is the ID of the user message item that will be created, thus a
 * `conversation.item.created` event will also be sent to the client.
 */
@Serializable
data class RealtimeBetaServerEventInputAudioBufferCommitted(
    /**
     * The unique ID of the server event.
     */
    @SerialName("event_id")
    val eventId: String,
    /**
     * The event type, must be `input_audio_buffer.committed`.
     */
    val type: String,
    @SerialName("previous_item_id")
    val previousItemId: String? = null,
    /**
     * The ID of the user message item that will be created.
     */
    @SerialName("item_id")
    val itemId: String
)
