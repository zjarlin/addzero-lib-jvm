// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Controls for how a thread will be truncated prior to the run. Use this to control the initial
 * context window of the run.
 */
@Serializable
data class TruncationObject(
    /**
     * The truncation strategy to use for the thread. The default is `auto`. If set to `last_messages`, the
     * thread will be truncated to the n most recent messages in the thread. When set to `auto`, messages
     * in the middle of the thread will be dropped to fit the context length of the model,
     * `max_prompt_tokens`.
     */
    val type: String,
    @SerialName("last_messages")
    val lastMessages: Int? = null
)
