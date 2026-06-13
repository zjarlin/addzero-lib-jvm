// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * The content part that is done.
 */
@Serializable
data class RealtimeBetaServerEventResponseContentPartDonePart(
    /**
     * The content type ("text", "audio").
     */
    val type: String? = null,
    /**
     * The text content (if type is "text").
     */
    val text: String? = null,
    /**
     * Base64-encoded audio data (if type is "audio").
     */
    val audio: String? = null,
    /**
     * The transcript of the audio (if type is "audio").
     */
    val transcript: String? = null
)
