// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

@Serializable
data class RealtimeConversationItemMessageSystemContentItem(
    /**
     * The content type. Always `input_text` for system messages.
     */
    val type: String? = null,
    /**
     * The text content.
     */
    val text: String? = null
)
