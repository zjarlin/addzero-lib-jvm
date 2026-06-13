// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A description of the chain of thought used by a reasoning model while generating a response. Be sure
 * to include these items in your `input` to the Responses API for subsequent turns of a conversation
 * if you are manually [managing context](/docs/guides/conversation-state).
 */
@Serializable
data class ReasoningItem(
    /**
     * The type of the object. Always `reasoning`.
     */
    val type: String,
    /**
     * The unique identifier of the reasoning content.
     */
    val id: String,
    @SerialName("encrypted_content")
    val encryptedContent: String? = null,
    /**
     * Reasoning summary content.
     */
    val summary: List<site.addzero.api.openai.models.SummaryTextContent>,
    /**
     * Reasoning text content.
     */
    val content: List<site.addzero.api.openai.models.ReasoningTextContent>? = null,
    /**
     * The status of the item. One of `in_progress`, `completed`, or `incomplete`. Populated when items are
     * returned via API.
     */
    val status: String? = null
)
