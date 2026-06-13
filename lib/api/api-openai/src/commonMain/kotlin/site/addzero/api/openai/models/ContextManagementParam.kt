// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ContextManagementParam(
    /**
     * The context management entry type. Currently only 'compaction' is supported.
     */
    val type: String,
    @SerialName("compact_threshold")
    val compactThreshold: Int? = null
)
