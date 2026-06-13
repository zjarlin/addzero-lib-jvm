// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * The output of a computer tool call.
 */
@Serializable
data class ComputerCallOutputItemParam(
    val id: String? = null,
    /**
     * The ID of the computer tool call that produced the output.
     */
    @SerialName("call_id")
    val callId: String,
    /**
     * The type of the computer tool call output. Always `computer_call_output`.
     */
    val type: String = "computer_call_output",
    val output: site.addzero.api.openai.models.ComputerScreenshotImage,
    @SerialName("acknowledged_safety_checks")
    val acknowledgedSafetyChecks: List<site.addzero.api.openai.models.ComputerCallSafetyCheckParam>? = null,
    val status: site.addzero.api.openai.models.FunctionCallItemStatus? = null
)
