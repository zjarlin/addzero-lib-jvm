// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ChatCompletionDeleted(
    /**
     * The type of object being deleted.
     */
    @SerialName("object")
    val objectType: String,
    /**
     * The ID of the chat completion that was deleted.
     */
    val id: String,
    /**
     * Whether the chat completion was deleted.
     */
    val deleted: Boolean
)
