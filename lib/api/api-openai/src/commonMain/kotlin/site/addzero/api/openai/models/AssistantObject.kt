// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * Represents an `assistant` that can call the model and use tools.
 */
@Serializable
data class AssistantObject(
    /**
     * The identifier, which can be referenced in API endpoints.
     */
    val id: String,
    /**
     * The object type, which is always `assistant`.
     */
    @SerialName("object")
    val objectType: String,
    /**
     * The Unix timestamp (in seconds) for when the assistant was created.
     */
    @SerialName("created_at")
    val createdAt: Long,
    val name: String?,
    val description: String?,
    /**
     * ID of the model to use. You can use the [List models](/docs/api-reference/models/list) API to see
     * all of your available models, or see our [Model overview](/docs/models) for descriptions of them.
     */
    val model: String,
    val instructions: String?,
    /**
     * A list of tool enabled on the assistant. There can be a maximum of 128 tools per assistant. Tools
     * can be of types `code_interpreter`, `file_search`, or `function`.
     */
    val tools: List<JsonElement>,
    @SerialName("tool_resources")
    val toolResources: site.addzero.api.openai.models.AssistantObjectToolResources? = null,
    val metadata: site.addzero.api.openai.models.Metadata?,
    val temperature: Double? = null,
    @SerialName("top_p")
    val topP: Double? = null,
    @SerialName("response_format")
    val responseFormat: site.addzero.api.openai.models.AssistantsApiResponseFormatOption? = null
)
