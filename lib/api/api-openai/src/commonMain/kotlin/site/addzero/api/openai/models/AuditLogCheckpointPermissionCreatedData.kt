// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * The payload used to create the checkpoint permission.
 */
@Serializable
data class AuditLogCheckpointPermissionCreatedData(
    /**
     * The ID of the project that the checkpoint permission was created for.
     */
    @SerialName("project_id")
    val projectId: String? = null,
    /**
     * The ID of the fine-tuned model checkpoint.
     */
    @SerialName("fine_tuned_model_checkpoint")
    val fineTunedModelCheckpoint: String? = null
)
