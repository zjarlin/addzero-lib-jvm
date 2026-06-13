// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * Request payload for creating a new group in the organization.
 */
@Serializable
data class CreateGroupBody(
    /**
     * Human readable name for the group.
     */
    val name: String
)
