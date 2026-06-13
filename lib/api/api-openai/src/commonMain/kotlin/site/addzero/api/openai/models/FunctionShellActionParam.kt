// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Commands and limits describing how to run the shell tool call.
 */
@Serializable
data class FunctionShellActionParam(
    /**
     * Ordered shell commands for the execution environment to run.
     */
    val commands: List<String>,
    @SerialName("timeout_ms")
    val timeoutMs: Int? = null,
    @SerialName("max_output_length")
    val maxOutputLength: Int? = null
)
