// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * Function tool call
 */
@Serializable
data class RunStepDetailsToolCallsFunctionObject(
    /**
     * The ID of the tool call object.
     */
    val id: String,
    /**
     * The type of tool call. This is always going to be `function` for this type of tool call.
     */
    val type: String,
    /**
     * The definition of the function that was called.
     */
    val function: site.addzero.api.openai.models.RunStepDetailsToolCallsFunctionObjectFunction
)
