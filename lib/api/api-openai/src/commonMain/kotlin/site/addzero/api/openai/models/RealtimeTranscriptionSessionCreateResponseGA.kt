// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A Realtime transcription session configuration object.
 */
@Serializable
data class RealtimeTranscriptionSessionCreateResponseGA(
    /**
     * The type of session. Always `transcription` for transcription sessions.
     */
    val type: String,
    /**
     * Unique identifier for the session that looks like `sess_1234567890abcdef`.
     */
    val id: String,
    /**
     * The object type. Always `realtime.transcription_session`.
     */
    @SerialName("object")
    val objectType: String,
    /**
     * Expiration timestamp for the session, in seconds since epoch.
     */
    @SerialName("expires_at")
    val expiresAt: Long? = null,
    /**
     * Additional fields to include in server outputs. - `item.input_audio_transcription.logprobs`: Include
     * logprobs for input audio transcription.
     */
    val include: List<String>? = null,
    /**
     * Configuration for input audio for the session.
     */
    val audio: site.addzero.api.openai.models.RealtimeTranscriptionSessionCreateResponseGAAudio? = null
)
