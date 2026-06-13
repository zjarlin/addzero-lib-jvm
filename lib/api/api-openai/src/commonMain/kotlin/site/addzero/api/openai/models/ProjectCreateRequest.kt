// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProjectCreateRequest(
    /**
     * The friendly name of the project, this name appears in reports.
     */
    val name: String,
    /**
     * Create the project with the specified data residency region. Your organization must have access to
     * Data residency functionality in order to use. See [data residency controls](/docs/guides/your-
     * data#data-residency-controls) to review the functionality and limitations of setting this field.
     */
    val geography: String? = null,
    /**
     * External key ID to associate with the project.
     */
    @SerialName("external_key_id")
    val externalKeyId: String? = null
)
