// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * The output of a code interpreter tool call that is text.
 */
@Serializable
data class CodeInterpreterTextOutput(
    /**
     * The type of the code interpreter text output. Always `logs`.
     */
    val type: String,
    /**
     * The logs of the code interpreter tool call.
     */
    val logs: String
)
