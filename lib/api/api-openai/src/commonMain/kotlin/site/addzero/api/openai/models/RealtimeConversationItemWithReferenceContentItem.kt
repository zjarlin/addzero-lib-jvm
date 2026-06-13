// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

@Serializable
data class RealtimeConversationItemWithReferenceContentItem(
    /**
     * The content type (`input_text`, `input_audio`, `item_reference`, `text`).
     */
    val type: String? = null,
    /**
     * The text content, used for `input_text` and `text` content types.
     */
    val text: String? = null,
    /**
     * ID of a previous conversation item to reference (for `item_reference` content types in
     * `response.create` events). These can reference both client and server created items.
     */
    val id: String? = null,
    /**
     * Base64-encoded audio bytes, used for `input_audio` content type.
     */
    val audio: String? = null,
    /**
     * The transcript of the audio, used for `input_audio` content type.
     */
    val transcript: String? = null
)
