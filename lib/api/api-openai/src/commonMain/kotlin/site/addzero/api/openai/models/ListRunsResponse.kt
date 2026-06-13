// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ListRunsResponse(
    @SerialName("object")
    val objectType: String,
    val data: List<site.addzero.api.openai.models.RunObject>,
    @SerialName("first_id")
    val firstId: String,
    @SerialName("last_id")
    val lastId: String,
    @SerialName("has_more")
    val hasMore: Boolean
)
