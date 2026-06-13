// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ApiKeyList(
    @SerialName("object")
    val objectType: String,
    val data: List<site.addzero.api.openai.models.AdminApiKey>,
    @SerialName("has_more")
    val hasMore: Boolean,
    @SerialName("first_id")
    val firstId: String? = null,
    @SerialName("last_id")
    val lastId: String? = null
)
