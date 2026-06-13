// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class CreateAssistantRequest(
    /**
     * ID of the model to use. You can use the [List models](/docs/api-reference/models/list) API to see
     * all of your available models, or see our [Model overview](/docs/models) for descriptions of them.
     */
    val model: JsonElement,
    val name: String? = null,
    val description: String? = null,
    val instructions: String? = null,
    @SerialName("reasoning_effort")
    val reasoningEffort: site.addzero.api.openai.models.ReasoningEffort? = null,
    /**
     * A list of tool enabled on the assistant. There can be a maximum of 128 tools per assistant. Tools
     * can be of types `code_interpreter`, `file_search`, or `function`.
     */
    val tools: List<JsonElement>? = null,
    @SerialName("tool_resources")
    val toolResources: site.addzero.api.openai.models.CreateAssistantRequestToolResources? = null,
    val metadata: site.addzero.api.openai.models.Metadata? = null,
    val temperature: Double? = null,
    @SerialName("top_p")
    val topP: Double? = null,
    @SerialName("response_format")
    val responseFormat: site.addzero.api.openai.models.AssistantsApiResponseFormatOption? = null
)
