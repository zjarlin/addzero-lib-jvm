// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Controls how much historical context is retained for the session.
 */
@Serializable
data class HistoryParam(
    /**
     * Enables chat users to access previous ChatKit threads. Defaults to true.
     */
    val enabled: Boolean? = null,
    /**
     * Number of recent ChatKit threads users have access to. Defaults to unlimited when unset.
     */
    @SerialName("recent_threads")
    val recentThreads: Int? = null
)
