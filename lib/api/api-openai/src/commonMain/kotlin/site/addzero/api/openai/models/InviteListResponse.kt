// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class InviteListResponse(
    /**
     * The object type, which is always `list`
     */
    @SerialName("object")
    val objectType: String,
    val data: List<site.addzero.api.openai.models.Invite>,
    /**
     * The first `invite_id` in the retrieved `list`
     */
    @SerialName("first_id")
    val firstId: String? = null,
    /**
     * The last `invite_id` in the retrieved `list`
     */
    @SerialName("last_id")
    val lastId: String? = null,
    /**
     * The `has_more` property is used for pagination to indicate there are additional results.
     */
    @SerialName("has_more")
    val hasMore: Boolean
)
