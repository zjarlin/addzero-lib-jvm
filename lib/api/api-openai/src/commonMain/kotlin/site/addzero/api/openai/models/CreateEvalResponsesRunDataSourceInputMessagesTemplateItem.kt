// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * ChatMessage
 */
@Serializable
data class CreateEvalResponsesRunDataSourceInputMessagesTemplateItem(
    /**
     * The role of the message (e.g. "system", "assistant", "user").
     */
    val role: String,
    /**
     * The content of the message.
     */
    val content: String
)
