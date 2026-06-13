// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

@Serializable
data class RealtimeConversationItemMessageAssistantContentItem(
    /**
     * The content type, `output_text` or `output_audio` depending on the session `output_modalities`
     * configuration.
     */
    val type: String? = null,
    /**
     * The text content.
     */
    val text: String? = null,
    /**
     * Base64-encoded audio bytes, these will be parsed as the format specified in the session output audio
     * type configuration. This defaults to PCM 16-bit 24kHz mono if not specified.
     */
    val audio: String? = null,
    /**
     * The transcript of the audio content, this will always be present if the output type is `audio`.
     */
    val transcript: String? = null
)
