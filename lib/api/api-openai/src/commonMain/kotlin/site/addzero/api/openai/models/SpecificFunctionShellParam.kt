// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * Forces the model to call the shell tool when a tool call is required.
 */
@Serializable
data class SpecificFunctionShellParam(
    /**
     * The tool to call. Always `shell`.
     */
    val type: String = "shell"
)
