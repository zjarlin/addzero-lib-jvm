// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A list of Response items.
 */
@Serializable
data class ResponseItemList(
    /**
     * The type of object returned, must be `list`.
     */
    @SerialName("object")
    val objectType: String,
    /**
     * A list of items used to generate this response.
     */
    val data: List<site.addzero.api.openai.models.ItemResource>,
    /**
     * Whether there are more items available.
     */
    @SerialName("has_more")
    val hasMore: Boolean,
    /**
     * The ID of the first item in the list.
     */
    @SerialName("first_id")
    val firstId: String,
    /**
     * The ID of the last item in the list.
     */
    @SerialName("last_id")
    val lastId: String
)
