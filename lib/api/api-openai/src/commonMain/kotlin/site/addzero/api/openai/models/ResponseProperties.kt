// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ResponseProperties(
    @SerialName("previous_response_id")
    val previousResponseId: String? = null,
    /**
     * Model ID used to generate the response, like `gpt-4o` or `o3`. OpenAI offers a wide range of models
     * with different capabilities, performance characteristics, and price points. Refer to the [model
     * guide](/docs/models) to browse and compare available models.
     */
    val model: site.addzero.api.openai.models.ModelIdsResponses? = null,
    val reasoning: site.addzero.api.openai.models.Reasoning? = null,
    val background: Boolean? = null,
    @SerialName("max_tool_calls")
    val maxToolCalls: Int? = null,
    val text: site.addzero.api.openai.models.ResponseTextParam? = null,
    val tools: site.addzero.api.openai.models.ToolsArray? = null,
    @SerialName("tool_choice")
    val toolChoice: site.addzero.api.openai.models.ToolChoiceParam? = null,
    val prompt: site.addzero.api.openai.models.Prompt? = null,
    val truncation: String? = null
)
