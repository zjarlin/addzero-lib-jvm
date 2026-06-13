// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * Realtime translation session fields that can be updated with `session.update`.
 */
@Serializable
data class RealtimeTranslationSessionUpdateRequest(
    /**
     * Configuration for translation input and output audio.
     */
    val audio: site.addzero.api.openai.models.RealtimeTranslationSessionUpdateRequestAudio? = null
)
