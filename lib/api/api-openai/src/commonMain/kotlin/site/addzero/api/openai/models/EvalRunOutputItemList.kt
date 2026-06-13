// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * An object representing a list of output items for an evaluation run.
 */
@Serializable
data class EvalRunOutputItemList(
    /**
     * The type of this object. It is always set to "list".
     */
    @SerialName("object")
    val objectType: String = "list",
    /**
     * An array of eval run output item objects.
     */
    val data: List<site.addzero.api.openai.models.EvalRunOutputItem>,
    /**
     * The identifier of the first eval run output item in the data array.
     */
    @SerialName("first_id")
    val firstId: String,
    /**
     * The identifier of the last eval run output item in the data array.
     */
    @SerialName("last_id")
    val lastId: String,
    /**
     * Indicates whether there are more eval run output items available.
     */
    @SerialName("has_more")
    val hasMore: Boolean
)
