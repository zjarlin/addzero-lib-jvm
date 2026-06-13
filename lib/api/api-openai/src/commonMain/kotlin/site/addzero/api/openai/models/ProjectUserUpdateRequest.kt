// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

@Serializable
data class ProjectUserUpdateRequest(
    /**
     * `owner` or `member`
     */
    val role: String? = null
)
