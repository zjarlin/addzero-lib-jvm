// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UsageResponse(
    @SerialName("object")
    val objectType: String,
    val data: List<site.addzero.api.openai.models.UsageTimeBucket>,
    @SerialName("has_more")
    val hasMore: Boolean,
    @SerialName("next_page")
    val nextPage: String?
)
