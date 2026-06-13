// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * Function message
 */
@Serializable
data class ChatCompletionRequestFunctionMessage(
    /**
     * The role of the messages author, in this case `function`.
     */
    val role: String,
    val content: String?,
    /**
     * The name of the function to call.
     */
    val name: String
)
