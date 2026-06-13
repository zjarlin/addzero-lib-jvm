// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

@Serializable
data class CreateConversationItemsRequest(
    /**
     * The items to add to the conversation. You may add up to 20 items at a time.
     */
    val items: List<site.addzero.api.openai.models.InputItem>
)
