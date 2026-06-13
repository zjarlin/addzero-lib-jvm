// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Returned when the text value of an input audio transcription content part is updated with
 * incremental transcription results.
 */
@Serializable
data class RealtimeServerEventConversationItemInputAudioTranscriptionDelta(
    /**
     * The unique ID of the server event.
     */
    @SerialName("event_id")
    val eventId: String,
    /**
     * The event type, must be `conversation.item.input_audio_transcription.delta`.
     */
    val type: String,
    /**
     * The ID of the item containing the audio that is being transcribed.
     */
    @SerialName("item_id")
    val itemId: String,
    /**
     * The index of the content part in the item's content array.
     */
    @SerialName("content_index")
    val contentIndex: Int? = null,
    /**
     * The text delta.
     */
    val delta: String? = null,
    val logprobs: List<site.addzero.api.openai.models.LogProbProperties>? = null
)
