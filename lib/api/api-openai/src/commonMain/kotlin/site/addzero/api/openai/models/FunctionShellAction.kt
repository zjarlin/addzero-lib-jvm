// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Execute a shell command.
 */
@Serializable
data class FunctionShellAction(
    val commands: List<String>,
    @SerialName("timeout_ms")
    val timeoutMs: Int?,
    @SerialName("max_output_length")
    val maxOutputLength: Int?
)
