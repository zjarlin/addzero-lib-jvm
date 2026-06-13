// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

@Serializable
data class ChatCompletionFunctions(
    /**
     * A description of what the function does, used by the model to choose when and how to call the
     * function.
     */
    val description: String? = null,
    /**
     * The name of the function to be called. Must be a-z, A-Z, 0-9, or contain underscores and dashes,
     * with a maximum length of 64.
     */
    val name: String,
    val parameters: site.addzero.api.openai.models.FunctionParameters? = null
)
