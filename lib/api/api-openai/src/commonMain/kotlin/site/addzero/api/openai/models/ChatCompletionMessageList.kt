// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * An object representing a list of chat completion messages.
 */
@Serializable
data class ChatCompletionMessageList(
    /**
     * The type of this object. It is always set to "list".
     */
    @SerialName("object")
    val objectType: String = "list",
    /**
     * An array of chat completion message objects.
     */
    val data: List<site.addzero.api.openai.models.ChatCompletionMessageListDataItem>,
    /**
     * The identifier of the first chat message in the data array.
     */
    @SerialName("first_id")
    val firstId: String,
    /**
     * The identifier of the last chat message in the data array.
     */
    @SerialName("last_id")
    val lastId: String,
    /**
     * Indicates whether there are more chat messages available.
     */
    @SerialName("has_more")
    val hasMore: Boolean
)
