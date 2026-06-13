// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

@Serializable
data class RealtimeTranslationSessionCreateRequestAudioOutput(
    /**
     * Target language for translated output audio and transcript deltas.
     */
    val language: String? = null
)
