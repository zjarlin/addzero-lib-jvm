// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * The content of a shell tool call output that was emitted.
 */
@Serializable
data class FunctionShellCallOutputContent(
    /**
     * The standard output that was captured.
     */
    val stdout: String,
    /**
     * The standard error output that was captured.
     */
    val stderr: String,
    /**
     * Represents either an exit outcome (with an exit code) or a timeout outcome for a shell call output
     * chunk.
     */
    val outcome: JsonElement,
    /**
     * The identifier of the actor that created the item.
     */
    @SerialName("created_by")
    val createdBy: String? = null
)
