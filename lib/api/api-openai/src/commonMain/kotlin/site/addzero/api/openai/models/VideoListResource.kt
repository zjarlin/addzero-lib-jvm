// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class VideoListResource(
    /**
     * The type of object returned, must be `list`.
     */
    @SerialName("object")
    val objectType: String = "list",
    /**
     * A list of items
     */
    val data: List<site.addzero.api.openai.models.VideoResource>,
    @SerialName("first_id")
    val firstId: String?,
    @SerialName("last_id")
    val lastId: String?,
    /**
     * Whether there are more items available.
     */
    @SerialName("has_more")
    val hasMore: Boolean
)
