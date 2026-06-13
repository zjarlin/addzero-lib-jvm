// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * Use this option to force the model to call a specific custom tool.
 */
@Serializable
data class ToolChoiceCustom(
    /**
     * For custom tool calling, the type is always `custom`.
     */
    val type: String,
    /**
     * The name of the custom tool to call.
     */
    val name: String
)
