// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ComputerToolCallOutputResource(
    /**
     * The type of the computer tool call output. Always `computer_call_output`.
     */
    val type: String = "computer_call_output",
    /**
     * The ID of the computer tool call output.
     */
    val id: String? = null,
    /**
     * The ID of the computer tool call that produced the output.
     */
    @SerialName("call_id")
    val callId: String,
    /**
     * The safety checks reported by the API that have been acknowledged by the developer.
     */
    @SerialName("acknowledged_safety_checks")
    val acknowledgedSafetyChecks: List<site.addzero.api.openai.models.ComputerCallSafetyCheckParam>? = null,
    val output: site.addzero.api.openai.models.ComputerScreenshotImage,
    /**
     * The status of the message input. One of `in_progress`, `completed`, or `incomplete`. Populated when
     * input items are returned via API.
     */
    val status: String? = null,
    /**
     * The identifier of the actor that created the item.
     */
    @SerialName("created_by")
    val createdBy: String? = null
)
