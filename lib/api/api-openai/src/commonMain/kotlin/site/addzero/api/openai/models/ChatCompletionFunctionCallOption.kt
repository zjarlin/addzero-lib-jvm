// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * Specifying a particular function via `{"name": "my_function"}` forces the model to call that
 * function.
 */
@Serializable
data class ChatCompletionFunctionCallOption(
    /**
     * The name of the function to call.
     */
    val name: String
)
