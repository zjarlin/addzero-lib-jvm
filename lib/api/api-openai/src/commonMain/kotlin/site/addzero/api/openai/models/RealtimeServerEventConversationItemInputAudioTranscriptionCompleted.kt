// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * This event is the output of audio transcription for user audio written to the user audio buffer.
 * Transcription begins when the input audio buffer is committed by the client or server (when VAD is
 * enabled). Transcription runs asynchronously with Response creation, so this event may come before or
 * after the Response events. Realtime API models accept audio natively, and thus input transcription
 * is a separate process run on a separate ASR (Automatic Speech Recognition) model. The transcript may
 * diverge somewhat from the model's interpretation, and should be treated as a rough guide.
 */
@Serializable
data class RealtimeServerEventConversationItemInputAudioTranscriptionCompleted(
    /**
     * The unique ID of the server event.
     */
    @SerialName("event_id")
    val eventId: String,
    /**
     * The event type, must be `conversation.item.input_audio_transcription.completed`.
     */
    val type: String,
    /**
     * The ID of the item containing the audio that is being transcribed.
     */
    @SerialName("item_id")
    val itemId: String,
    /**
     * The index of the content part containing the audio.
     */
    @SerialName("content_index")
    val contentIndex: Int,
    /**
     * The transcribed text.
     */
    val transcript: String,
    val logprobs: List<site.addzero.api.openai.models.LogProbProperties>? = null,
    /**
     * Usage statistics for the transcription, this is billed according to the ASR model's pricing rather
     * than the realtime model's pricing.
     */
    val usage: JsonElement
)
