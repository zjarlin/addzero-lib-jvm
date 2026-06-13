// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateEvalResponsesRunDataSourceSamplingParams(
    @SerialName("reasoning_effort")
    val reasoningEffort: site.addzero.api.openai.models.ReasoningEffort? = null,
    /**
     * A higher temperature increases randomness in the outputs.
     */
    val temperature: Double? = 1.0,
    /**
     * The maximum number of tokens in the generated output.
     */
    @SerialName("max_completion_tokens")
    val maxCompletionTokens: Int? = null,
    /**
     * An alternative to temperature for nucleus sampling; 1.0 includes all tokens.
     */
    @SerialName("top_p")
    val topP: Double? = 1.0,
    /**
     * A seed value to initialize the randomness, during sampling.
     */
    val seed: Int? = 42,
    /**
     * An array of tools the model may call while generating a response. You can specify which tool to use
     * by setting the `tool_choice` parameter. The two categories of tools you can provide the model are: -
     * **Built-in tools**: Tools that are provided by OpenAI that extend the model's capabilities, like
     * [web search](/docs/guides/tools-web-search) or [file search](/docs/guides/tools-file-search). Learn
     * more about [built-in tools](/docs/guides/tools). - **Function calls (custom tools)**: Functions that
     * are defined by you, enabling the model to call your own code. Learn more about [function
     * calling](/docs/guides/function-calling).
     */
    val tools: List<site.addzero.api.openai.models.Tool>? = null,
    /**
     * Configuration options for a text response from the model. Can be plain text or structured JSON data.
     * Learn more: - [Text inputs and outputs](/docs/guides/text) - [Structured
     * Outputs](/docs/guides/structured-outputs)
     */
    val text: site.addzero.api.openai.models.CreateEvalResponsesRunDataSourceSamplingParamsText? = null
)
