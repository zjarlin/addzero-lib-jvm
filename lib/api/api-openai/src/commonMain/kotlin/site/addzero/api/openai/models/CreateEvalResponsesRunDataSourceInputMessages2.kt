// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * InputMessagesItemReference
 */
@Serializable
data class CreateEvalResponsesRunDataSourceInputMessages2(
    /**
     * The type of input messages. Always `item_reference`.
     */
    val type: String,
    /**
     * A reference to a variable in the `item` namespace. Ie, "item.name"
     */
    @SerialName("item_reference")
    val itemReference: String
)
