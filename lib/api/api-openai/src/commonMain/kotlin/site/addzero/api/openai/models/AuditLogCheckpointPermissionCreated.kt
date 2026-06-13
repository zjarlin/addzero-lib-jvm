// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * The project and fine-tuned model checkpoint that the checkpoint permission was created for.
 */
@Serializable
data class AuditLogCheckpointPermissionCreated(
    /**
     * The ID of the checkpoint permission.
     */
    val id: String? = null,
    /**
     * The payload used to create the checkpoint permission.
     */
    val data: site.addzero.api.openai.models.AuditLogCheckpointPermissionCreatedData? = null
)
