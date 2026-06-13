// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BatchErrors(
    /**
     * The object type, which is always `list`.
     */
    @SerialName("object")
    val objectType: String? = null,
    val data: List<site.addzero.api.openai.models.BatchErrorsDataItem>? = null
)
