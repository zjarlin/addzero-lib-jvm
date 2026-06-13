// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Execute a shell command on the server.
 */
@Serializable
data class LocalShellExecAction(
    /**
     * The type of the local shell action. Always `exec`.
     */
    val type: String = "exec",
    /**
     * The command to run.
     */
    val command: List<String>,
    @SerialName("timeout_ms")
    val timeoutMs: Int? = null,
    @SerialName("working_directory")
    val workingDirectory: String? = null,
    /**
     * Environment variables to set for the command.
     */
    val env: Map<String, String>,
    val user: String? = null
)
