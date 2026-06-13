// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * A function tool that can be used to generate a response.
 */
@Serializable
data class ChatCompletionTool(
    /**
     * The type of the tool. Currently, only `function` is supported.
     */
    val type: String,
    val function: site.addzero.api.openai.models.FunctionObject
)
