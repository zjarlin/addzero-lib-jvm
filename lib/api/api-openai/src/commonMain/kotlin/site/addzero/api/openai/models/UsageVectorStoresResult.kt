// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * The aggregated vector stores usage details of the specific time bucket.
 */
@Serializable
data class UsageVectorStoresResult(
    @SerialName("object")
    val objectType: String,
    /**
     * The vector stores usage in bytes.
     */
    @SerialName("usage_bytes")
    val usageBytes: Int,
    @SerialName("project_id")
    val projectId: String? = null
)
