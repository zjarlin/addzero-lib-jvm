// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * The definition of the function that was called.
 */
@Serializable
data class RunStepDeltaStepDetailsToolCallsFunctionObjectFunction(
    /**
     * The name of the function.
     */
    val name: String? = null,
    /**
     * The arguments passed to the function.
     */
    val arguments: String? = null,
    val output: String? = null
)
