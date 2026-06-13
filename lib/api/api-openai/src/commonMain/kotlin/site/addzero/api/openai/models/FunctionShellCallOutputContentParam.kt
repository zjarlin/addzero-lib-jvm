// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * Captured stdout and stderr for a portion of a shell tool call output.
 */
@Serializable
data class FunctionShellCallOutputContentParam(
    /**
     * Captured stdout output for the shell call.
     */
    val stdout: String,
    /**
     * Captured stderr output for the shell call.
     */
    val stderr: String,
    /**
     * The exit or timeout outcome associated with this shell call.
     */
    val outcome: site.addzero.api.openai.models.FunctionShellCallOutputOutcomeParam
)
