// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * **SIP Only:** Returned when an DTMF event is received. A DTMF event is a message that represents a
 * telephone keypad press (0–9, *, #, A–D). The `event` property is the keypad that the user press. The
 * `received_at` is the UTC Unix Timestamp that the server received the event.
 */
@Serializable
data class RealtimeServerEventInputAudioBufferDtmfEventReceived(
    /**
     * The event type, must be `input_audio_buffer.dtmf_event_received`.
     */
    val type: String,
    /**
     * The telephone keypad that was pressed by the user.
     */
    val event: String,
    /**
     * UTC Unix Timestamp when DTMF Event was received by server.
     */
    @SerialName("received_at")
    val receivedAt: Int
)
