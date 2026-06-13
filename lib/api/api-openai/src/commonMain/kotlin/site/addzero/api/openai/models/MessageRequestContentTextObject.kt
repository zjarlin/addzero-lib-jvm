// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * The text content that is part of a message.
 */
@Serializable
data class MessageRequestContentTextObject(
    /**
     * Always `text`.
     */
    val type: String,
    /**
     * Text content to be sent to the model
     */
    val text: String
)
