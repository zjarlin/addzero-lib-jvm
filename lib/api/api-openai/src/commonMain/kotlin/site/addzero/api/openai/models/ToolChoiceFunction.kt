// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * Use this option to force the model to call a specific function.
 */
@Serializable
data class ToolChoiceFunction(
    /**
     * For function calling, the type is always `function`.
     */
    val type: String,
    /**
     * The name of the function to call.
     */
    val name: String
)
