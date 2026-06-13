// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Returned when an input audio transcription segment is identified for an item.
 */
@Serializable
data class RealtimeBetaServerEventConversationItemInputAudioTranscriptionSegment(
    /**
     * The unique ID of the server event.
     */
    @SerialName("event_id")
    val eventId: String,
    /**
     * The event type, must be `conversation.item.input_audio_transcription.segment`.
     */
    val type: String,
    /**
     * The ID of the item containing the input audio content.
     */
    @SerialName("item_id")
    val itemId: String,
    /**
     * The index of the input audio content part within the item.
     */
    @SerialName("content_index")
    val contentIndex: Int,
    /**
     * The text for this segment.
     */
    val text: String,
    /**
     * The segment identifier.
     */
    val id: String,
    /**
     * The detected speaker label for this segment.
     */
    val speaker: String,
    /**
     * Start time of the segment in seconds.
     */
    val start: Double,
    /**
     * End time of the segment in seconds.
     */
    val end: Double
)
