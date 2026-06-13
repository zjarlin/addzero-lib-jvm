// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * A custom tool that processes input using a specified format. Learn more about [custom
 * tools](/docs/guides/function-calling#custom-tools)
 */
@Serializable
data class CustomToolParam(
    /**
     * The type of the custom tool. Always `custom`.
     */
    val type: String = "custom",
    /**
     * The name of the custom tool, used to identify it in tool calls.
     */
    val name: String,
    /**
     * Optional description of the custom tool, used to provide more context.
     */
    val description: String? = null,
    /**
     * The input format for the custom tool. Default is unconstrained text.
     */
    val format: JsonElement? = null,
    /**
     * Whether this tool should be deferred and discovered via tool search.
     */
    @SerialName("defer_loading")
    val deferLoading: Boolean? = null
)
