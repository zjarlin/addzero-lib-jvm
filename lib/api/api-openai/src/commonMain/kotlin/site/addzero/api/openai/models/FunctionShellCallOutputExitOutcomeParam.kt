// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Indicates that the shell commands finished and returned an exit code.
 */
@Serializable
data class FunctionShellCallOutputExitOutcomeParam(
    /**
     * The outcome type. Always `exit`.
     */
    val type: String = "exit",
    /**
     * The exit code returned by the shell process.
     */
    @SerialName("exit_code")
    val exitCode: Int
)
