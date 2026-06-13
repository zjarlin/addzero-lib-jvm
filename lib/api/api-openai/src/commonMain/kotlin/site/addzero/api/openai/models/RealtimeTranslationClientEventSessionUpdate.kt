// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Send this event to update the translation session configuration. Translation sessions support
 * updates to `audio.output.language`, `audio.input.transcription`, and `audio.input.noise_reduction`.
 */
@Serializable
data class RealtimeTranslationClientEventSessionUpdate(
    /**
     * Optional client-generated ID used to identify this event.
     */
    @SerialName("event_id")
    val eventId: String? = null,
    /**
     * The event type, must be `session.update`.
     */
    val type: String,
    /**
     * Translation session fields to update. The session `type` and `model` are set at creation and cannot
     * be changed with `session.update`.
     */
    val session: site.addzero.api.openai.models.RealtimeTranslationSessionUpdateRequest
)
