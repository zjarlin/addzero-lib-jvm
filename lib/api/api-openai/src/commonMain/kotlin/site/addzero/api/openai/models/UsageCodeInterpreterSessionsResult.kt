// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * The aggregated code interpreter sessions usage details of the specific time bucket.
 */
@Serializable
data class UsageCodeInterpreterSessionsResult(
    @SerialName("object")
    val objectType: String,
    /**
     * The number of code interpreter sessions.
     */
    @SerialName("num_sessions")
    val numSessions: Int,
    @SerialName("project_id")
    val projectId: String? = null
)
