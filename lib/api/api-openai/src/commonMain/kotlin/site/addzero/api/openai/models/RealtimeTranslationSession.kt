// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A Realtime translation session. Translation sessions continuously translate input audio into the
 * configured output language.
 */
@Serializable
data class RealtimeTranslationSession(
    /**
     * Unique identifier for the session that looks like `sess_1234567890abcdef`.
     */
    val id: String,
    /**
     * The session type. Always `translation` for Realtime translation sessions.
     */
    val type: String,
    /**
     * Expiration timestamp for the session, in seconds since epoch.
     */
    @SerialName("expires_at")
    val expiresAt: Long,
    /**
     * The Realtime translation model used for this session. This field is set at session creation and
     * cannot be changed with `session.update`.
     */
    val model: String,
    /**
     * Configuration for translation input and output audio.
     */
    val audio: site.addzero.api.openai.models.RealtimeTranslationSessionAudio
)
