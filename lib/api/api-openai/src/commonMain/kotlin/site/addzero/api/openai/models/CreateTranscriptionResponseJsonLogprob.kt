// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

@Serializable
data class CreateTranscriptionResponseJsonLogprob(
    /**
     * The token in the transcription.
     */
    val token: String? = null,
    /**
     * The log probability of the token.
     */
    val logprob: Double? = null,
    /**
     * The bytes of the token.
     */
    val bytes: List<Double>? = null
)
