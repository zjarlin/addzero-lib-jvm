// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Paginated list of organization groups.
 */
@Serializable
data class GroupListResource(
    /**
     * Always `list`.
     */
    @SerialName("object")
    val objectType: String,
    /**
     * Groups returned in the current page.
     */
    val data: List<site.addzero.api.openai.models.GroupResponse>,
    /**
     * Whether additional groups are available when paginating.
     */
    @SerialName("has_more")
    val hasMore: Boolean,
    /**
     * Cursor to fetch the next page of results, or `null` if there are no more results.
     */
    val next: String?
)
