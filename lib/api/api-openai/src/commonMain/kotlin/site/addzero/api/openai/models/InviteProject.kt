// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

@Serializable
data class InviteProject(
    /**
     * Project's public ID
     */
    val id: String,
    /**
     * Project membership role
     */
    val role: String
)
