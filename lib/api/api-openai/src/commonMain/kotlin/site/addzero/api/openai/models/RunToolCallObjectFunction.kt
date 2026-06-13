// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * The function definition.
 */
@Serializable
data class RunToolCallObjectFunction(
    /**
     * The name of the function.
     */
    val name: String,
    /**
     * The arguments that the model expects you to pass to the function.
     */
    val arguments: String
)
