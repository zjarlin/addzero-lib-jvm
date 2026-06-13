// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class ConversationResource(
    /**
     * The unique ID of the conversation.
     */
    val id: String,
    /**
     * The object type, which is always `conversation`.
     */
    @SerialName("object")
    val objectType: String = "conversation",
    /**
     * Set of 16 key-value pairs that can be attached to an object. This can be useful for storing
     * additional information about the object in a structured format, and querying for objects via API or
     * the dashboard. Keys are strings with a maximum length of 64 characters. Values are strings with a
     * maximum length of 512 characters.
     */
    val metadata: JsonElement,
    /**
     * The time at which the conversation was created, measured in seconds since the Unix epoch.
     */
    @SerialName("created_at")
    val createdAt: Long
)
