// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

@Serializable
data class EvalRunOutputItemSampleOutputItem(
    /**
     * The role of the message (e.g. "system", "assistant", "user").
     */
    val role: String? = null,
    /**
     * The content of the message.
     */
    val content: String? = null
)
