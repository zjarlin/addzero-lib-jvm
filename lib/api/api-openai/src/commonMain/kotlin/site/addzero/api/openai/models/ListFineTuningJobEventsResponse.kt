// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ListFineTuningJobEventsResponse(
    val data: List<site.addzero.api.openai.models.FineTuningJobEvent>,
    @SerialName("object")
    val objectType: String,
    @SerialName("has_more")
    val hasMore: Boolean
)
