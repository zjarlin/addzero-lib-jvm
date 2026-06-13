// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ListBatchesResponse(
    val data: List<site.addzero.api.openai.models.Batch>,
    @SerialName("first_id")
    val firstId: String? = null,
    @SerialName("last_id")
    val lastId: String? = null,
    @SerialName("has_more")
    val hasMore: Boolean,
    @SerialName("object")
    val objectType: String
)
