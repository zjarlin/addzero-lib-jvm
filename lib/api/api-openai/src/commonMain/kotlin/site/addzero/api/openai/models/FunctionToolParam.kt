// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FunctionToolParam(
    val name: String,
    val description: String? = null,
    val parameters: site.addzero.api.openai.models.EmptyModelParam? = null,
    val strict: Boolean? = null,
    val type: String = "function",
    /**
     * Whether this function should be deferred and discovered via tool search.
     */
    @SerialName("defer_loading")
    val deferLoading: Boolean? = null
)
