// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ListPaginatedFineTuningJobsResponse(
    val data: List<site.addzero.api.openai.models.FineTuningJob>,
    @SerialName("has_more")
    val hasMore: Boolean,
    @SerialName("object")
    val objectType: String
)
