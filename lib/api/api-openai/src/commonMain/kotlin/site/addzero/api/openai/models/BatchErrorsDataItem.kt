// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

@Serializable
data class BatchErrorsDataItem(
    /**
     * An error code identifying the error type.
     */
    val code: String? = null,
    /**
     * A human-readable message providing more details about the error.
     */
    val message: String? = null,
    val param: String? = null,
    val line: Int? = null
)
