// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * Indicates that the shell call exceeded its configured time limit.
 */
@Serializable
data class FunctionShellCallOutputTimeoutOutcomeParam(
    /**
     * The outcome type. Always `timeout`.
     */
    val type: String = "timeout"
)
