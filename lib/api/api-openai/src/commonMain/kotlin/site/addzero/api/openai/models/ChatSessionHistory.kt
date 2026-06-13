// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * History retention preferences returned for the session.
 */
@Serializable
data class ChatSessionHistory(
    /**
     * Indicates if chat history is persisted for the session.
     */
    val enabled: Boolean,
    @SerialName("recent_threads")
    val recentThreads: Int?
)
