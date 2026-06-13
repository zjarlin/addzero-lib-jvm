// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

@Serializable
data class Error(
    val code: String?,
    val message: String,
    val param: String?,
    val type: String
)
