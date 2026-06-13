// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * Configuration for translation input and output audio.
 */
@Serializable
data class RealtimeTranslationSessionCreateRequestAudio(
    val input: site.addzero.api.openai.models.RealtimeTranslationSessionCreateRequestAudioInput? = null,
    val output: site.addzero.api.openai.models.RealtimeTranslationSessionCreateRequestAudioOutput? = null
)
