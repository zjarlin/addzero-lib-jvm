// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * The PCM audio format. Only a 24kHz sample rate is supported.
 */
@Serializable
data class RealtimeAudioFormats2(
    /**
     * The audio format. Always `audio/pcm`.
     */
    val type: String? = null,
    /**
     * The sample rate of the audio. Always `24000`.
     */
    val rate: Int? = null
)
