// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * An internal identifier for an item to reference.
 */
@Serializable
data class ItemReferenceParam(
    val type: String? = null,
    /**
     * The ID of the item to reference.
     */
    val id: String
)
