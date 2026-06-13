// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * Optional source-language transcription. When configured, the server emits
 * `session.input_transcript.delta` events. Translation itself still runs from the input audio stream.
 */
@Serializable
data class RealtimeTranslationSessionCreateRequestAudioInputTranscription(
    /**
     * The transcription model to use for source transcript deltas.
     */
    val model: String
)
