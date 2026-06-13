// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A tool call to a computer use tool. See the [computer use guide](/docs/guides/tools-computer-use)
 * for more information.
 */
@Serializable
data class ComputerToolCall(
    /**
     * The type of the computer call. Always `computer_call`.
     */
    val type: String = "computer_call",
    /**
     * The unique ID of the computer call.
     */
    val id: String,
    /**
     * An identifier used when responding to the tool call with output.
     */
    @SerialName("call_id")
    val callId: String,
    val action: site.addzero.api.openai.models.ComputerAction? = null,
    val actions: site.addzero.api.openai.models.ComputerActionList? = null,
    /**
     * The pending safety checks for the computer call.
     */
    @SerialName("pending_safety_checks")
    val pendingSafetyChecks: List<site.addzero.api.openai.models.ComputerCallSafetyCheckParam>,
    /**
     * The status of the item. One of `in_progress`, `completed`, or `incomplete`. Populated when items are
     * returned via API.
     */
    val status: String
)
