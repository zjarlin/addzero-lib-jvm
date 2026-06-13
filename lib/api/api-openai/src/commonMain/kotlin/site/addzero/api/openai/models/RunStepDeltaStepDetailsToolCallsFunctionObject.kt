// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * Function tool call
 */
@Serializable
data class RunStepDeltaStepDetailsToolCallsFunctionObject(
    /**
     * The index of the tool call in the tool calls array.
     */
    val index: Int,
    /**
     * The ID of the tool call object.
     */
    val id: String? = null,
    /**
     * The type of tool call. This is always going to be `function` for this type of tool call.
     */
    val type: String,
    /**
     * The definition of the function that was called.
     */
    val function: site.addzero.api.openai.models.RunStepDeltaStepDetailsToolCallsFunctionObjectFunction? = null
)
