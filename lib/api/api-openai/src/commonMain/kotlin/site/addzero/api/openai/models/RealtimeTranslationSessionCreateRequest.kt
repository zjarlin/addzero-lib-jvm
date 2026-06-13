// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * Realtime translation session configuration. Translation sessions stream source audio in and
 * translated audio plus transcript deltas out continuously.
 */
@Serializable
data class RealtimeTranslationSessionCreateRequest(
    /**
     * The Realtime translation model used for this session.
     */
    val model: String,
    /**
     * Configuration for translation input and output audio.
     */
    val audio: site.addzero.api.openai.models.RealtimeTranslationSessionCreateRequestAudio? = null
)
