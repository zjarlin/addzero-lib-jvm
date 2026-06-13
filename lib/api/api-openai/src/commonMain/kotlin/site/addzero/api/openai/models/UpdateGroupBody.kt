// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * Request payload for updating the details of an existing group.
 */
@Serializable
data class UpdateGroupBody(
    /**
     * New display name for the group.
     */
    val name: String
)
