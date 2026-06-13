// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * The logs output from the code interpreter.
 */
@Serializable
data class CodeInterpreterOutputLogs(
    /**
     * The type of the output. Always `logs`.
     */
    val type: String = "logs",
    /**
     * The logs output from the code interpreter.
     */
    val logs: String
)
