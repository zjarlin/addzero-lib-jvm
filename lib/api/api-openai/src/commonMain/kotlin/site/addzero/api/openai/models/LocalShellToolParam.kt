// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * A tool that allows the model to execute shell commands in a local environment.
 */
@Serializable
data class LocalShellToolParam(
    /**
     * The type of the local shell tool. Always `local_shell`.
     */
    val type: String = "local_shell"
)
