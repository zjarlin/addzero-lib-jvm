// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * Tool call objects
 */
@Serializable
data class RunToolCallObject(
    /**
     * The ID of the tool call. This ID must be referenced when you submit the tool outputs in using the
     * [Submit tool outputs to run](/docs/api-reference/runs/submitToolOutputs) endpoint.
     */
    val id: String,
    /**
     * The type of tool call the output is required for. For now, this is always `function`.
     */
    val type: String,
    /**
     * The function definition.
     */
    val function: site.addzero.api.openai.models.RunToolCallObjectFunction
)
