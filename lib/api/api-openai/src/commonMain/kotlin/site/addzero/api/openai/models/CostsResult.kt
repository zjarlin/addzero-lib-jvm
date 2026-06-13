// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * The aggregated costs details of the specific time bucket.
 */
@Serializable
data class CostsResult(
    @SerialName("object")
    val objectType: String,
    /**
     * The monetary value in its associated currency.
     */
    val amount: site.addzero.api.openai.models.CostsResultAmount? = null,
    @SerialName("line_item")
    val lineItem: String? = null,
    @SerialName("project_id")
    val projectId: String? = null,
    @SerialName("api_key_id")
    val apiKeyId: String? = null,
    val quantity: Double? = null
)
