// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * Optional input noise reduction. Set to `null` to disable it.
 */
@Serializable
data class RealtimeTranslationSessionUpdateRequestAudioInputNoiseReduction(
    val type: site.addzero.api.openai.models.NoiseReductionType
)
