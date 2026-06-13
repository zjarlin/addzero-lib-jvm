// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * Details of the transcription error.
 */
@Serializable
data class RealtimeBetaServerEventConversationItemInputAudioTranscriptionFailedError(
    /**
     * The type of error.
     */
    val type: String? = null,
    /**
     * Error code, if any.
     */
    val code: String? = null,
    /**
     * A human-readable error message.
     */
    val message: String? = null,
    /**
     * Parameter related to the error, if any.
     */
    val param: String? = null
)
