// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * The text content that is part of a message.
 */
@Serializable
data class MessageContentTextObject(
    /**
     * Always `text`.
     */
    val type: String,
    val text: site.addzero.api.openai.models.MessageContentTextObjectText
)
