// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class TokenCountsBody(
    val model: String? = null,
    val input: JsonElement? = null,
    @SerialName("previous_response_id")
    val previousResponseId: String? = null,
    val tools: List<site.addzero.api.openai.models.Tool>? = null,
    val text: site.addzero.api.openai.models.ResponseTextParam? = null,
    val reasoning: site.addzero.api.openai.models.Reasoning? = null,
    /**
     * The truncation strategy to use for the model response. - `auto`: If the input to this Response
     * exceeds the model's context window size, the model will truncate the response to fit the context
     * window by dropping items from the beginning of the conversation. - `disabled` (default): If the
     * input size will exceed the context window size for a model, the request will fail with a 400 error.
     */
    val truncation: site.addzero.api.openai.models.TruncationEnum? = null,
    val instructions: String? = null,
    val conversation: site.addzero.api.openai.models.ConversationParam? = null,
    @SerialName("tool_choice")
    val toolChoice: site.addzero.api.openai.models.ToolChoiceParam? = null,
    @SerialName("parallel_tool_calls")
    val parallelToolCalls: Boolean? = null
)
