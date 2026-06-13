// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * Function tool
 */
@Serializable
data class RealtimeFunctionTool(
    /**
     * The type of the tool, i.e. `function`.
     */
    val type: String? = null,
    /**
     * The name of the function.
     */
    val name: String? = null,
    /**
     * The description of the function, including guidance on when and how to call it, and guidance about
     * what to tell the user when calling (if anything).
     */
    val description: String? = null,
    /**
     * Parameters of the function in JSON Schema.
     */
    val parameters: Map<String, JsonElement>? = null
)
