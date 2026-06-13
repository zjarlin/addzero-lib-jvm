// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * Configuration for input audio noise reduction.
 */
@Serializable
data class RealtimeSessionCreateResponseAudioInputNoiseReduction(
    val type: site.addzero.api.openai.models.NoiseReductionType? = null
)
