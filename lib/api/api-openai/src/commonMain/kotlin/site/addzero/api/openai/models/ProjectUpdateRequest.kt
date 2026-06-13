// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProjectUpdateRequest(
    /**
     * The updated name of the project, this name appears in reports.
     */
    val name: String? = null,
    /**
     * External key ID to associate with the project.
     */
    @SerialName("external_key_id")
    val externalKeyId: String? = null,
    /**
     * Geography for the project.
     */
    val geography: String? = null
)
