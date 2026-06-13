// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * Configuration for translation input and output audio.
 */
@Serializable
data class RealtimeTranslationSessionAudio(
    val input: site.addzero.api.openai.models.RealtimeTranslationSessionAudioInput? = null,
    val output: site.addzero.api.openai.models.RealtimeTranslationSessionAudioOutput? = null
)
