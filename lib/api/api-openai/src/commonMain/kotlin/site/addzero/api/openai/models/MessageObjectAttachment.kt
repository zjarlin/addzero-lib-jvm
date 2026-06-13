// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class MessageObjectAttachment(
    /**
     * The ID of the file to attach to the message.
     */
    @SerialName("file_id")
    val fileId: String? = null,
    /**
     * The tools to add this file to.
     */
    val tools: List<JsonElement>? = null
)
