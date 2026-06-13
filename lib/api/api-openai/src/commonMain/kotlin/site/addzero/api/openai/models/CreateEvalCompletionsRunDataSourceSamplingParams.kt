// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class CreateEvalCompletionsRunDataSourceSamplingParams(
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
     * An object specifying the format that the model must output. Setting to `{ "type": "json_schema",
     * "json_schema": {...} }` enables Structured Outputs which ensures the model will match your supplied
     * JSON schema. Learn more in the [Structured Outputs guide](/docs/guides/structured-outputs). Setting
     * to `{ "type": "json_object" }` enables the older JSON mode, which ensures the message the model
     * generates is valid JSON. Using `json_schema` is preferred for models that support it.
     */
    @SerialName("response_format")
    val responseFormat: JsonElement? = null,
    /**
     * A list of tools the model may call. Currently, only functions are supported as a tool. Use this to
     * provide a list of functions the model may generate JSON inputs for. A max of 128 functions are
     * supported.
     */
    val tools: List<site.addzero.api.openai.models.ChatCompletionTool>? = null
)
