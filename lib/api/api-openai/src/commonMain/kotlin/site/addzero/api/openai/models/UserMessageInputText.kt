// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * Text block that a user contributed to the thread.
 */
@Serializable
data class UserMessageInputText(
    /**
     * Type discriminator that is always `input_text`.
     */
    val type: String = "input_text",
    /**
     * Plain-text content supplied by the user.
     */
    val text: String
)
