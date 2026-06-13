// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * Configuration for input audio noise reduction. This can be set to `null` to turn off. Noise
 * reduction filters audio added to the input audio buffer before it is sent to VAD and the model.
 * Filtering the audio can improve VAD and turn detection accuracy (reducing false positives) and model
 * performance by improving perception of the input audio.
 */
@Serializable
data class RealtimeCallCreateRequestSessionAudioInputNoiseReduction(
    val type: site.addzero.api.openai.models.NoiseReductionType? = null
)
