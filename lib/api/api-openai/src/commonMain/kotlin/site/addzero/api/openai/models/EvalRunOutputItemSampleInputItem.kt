// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * An input message.
 */
@Serializable
data class EvalRunOutputItemSampleInputItem(
    /**
     * The role of the message sender (e.g., system, user, developer).
     */
    val role: String,
    /**
     * The content of the message.
     */
    val content: String
)
