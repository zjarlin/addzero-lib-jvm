// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * Represents a transcription response returned by model, based on the provided input.
 */
@Serializable
data class CreateTranscriptionResponseJson(
    /**
     * The transcribed text.
     */
    val text: String,
    /**
     * The log probabilities of the tokens in the transcription. Only returned with the models
     * `gpt-4o-transcribe` and `gpt-4o-mini-transcribe` if `logprobs` is added to the `include` array.
     */
    val logprobs: List<site.addzero.api.openai.models.CreateTranscriptionResponseJsonLogprob>? = null,
    /**
     * Token usage statistics for the request.
     */
    val usage: JsonElement? = null
)
