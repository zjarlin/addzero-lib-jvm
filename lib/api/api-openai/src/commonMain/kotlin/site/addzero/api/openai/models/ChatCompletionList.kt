// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * An object representing a list of Chat Completions.
 */
@Serializable
data class ChatCompletionList(
    /**
     * The type of this object. It is always set to "list".
     */
    @SerialName("object")
    val objectType: String = "list",
    /**
     * An array of chat completion objects.
     */
    val data: List<site.addzero.api.openai.models.CreateChatCompletionResponse>,
    /**
     * The identifier of the first chat completion in the data array.
     */
    @SerialName("first_id")
    val firstId: String,
    /**
     * The identifier of the last chat completion in the data array.
     */
    @SerialName("last_id")
    val lastId: String,
    /**
     * Indicates whether there are more Chat Completions available.
     */
    @SerialName("has_more")
    val hasMore: Boolean
)
