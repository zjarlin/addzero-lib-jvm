// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * Learn about [text inputs](/docs/guides/text-generation).
 */
@Serializable
data class ChatCompletionRequestMessageContentPartText(
    /**
     * The type of the content part.
     */
    val type: String,
    /**
     * The text content.
     */
    val text: String
)
