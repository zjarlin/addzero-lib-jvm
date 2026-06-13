// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * Defines a function in your own code the model can choose to call. Learn more about [function
 * calling](https://platform.openai.com/docs/guides/function-calling).
 */
@Serializable
data class FunctionTool(
    /**
     * The type of the function tool. Always `function`.
     */
    val type: String = "function",
    /**
     * The name of the function to call.
     */
    val name: String,
    val description: String? = null,
    val parameters: Map<String, JsonElement>?,
    val strict: Boolean?,
    /**
     * Whether this function is deferred and loaded via tool search.
     */
    @SerialName("defer_loading")
    val deferLoading: Boolean? = null
)
