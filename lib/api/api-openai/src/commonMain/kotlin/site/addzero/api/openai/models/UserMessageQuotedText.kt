// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * Quoted snippet that the user referenced in their message.
 */
@Serializable
data class UserMessageQuotedText(
    /**
     * Type discriminator that is always `quoted_text`.
     */
    val type: String = "quoted_text",
    /**
     * Quoted text content.
     */
    val text: String
)
