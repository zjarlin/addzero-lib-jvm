// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * Configuration for input audio transcription, defaults to off and can be set to `null` to turn off
 * once on. Input audio transcription is not native to the model, since the model consumes audio
 * directly. Transcription runs asynchronously and should be treated as rough guidance rather than the
 * representation understood by the model.
 */
@Serializable
data class RealtimeSessionCreateRequestInputAudioTranscription(
    /**
     * The model to use for transcription.
     */
    val model: String? = null
)
