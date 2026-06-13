// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * The `checkpoint.permission` object represents a permission for a fine-tuned model checkpoint.
 */
@Serializable
data class FineTuningCheckpointPermission(
    /**
     * The permission identifier, which can be referenced in the API endpoints.
     */
    val id: String,
    /**
     * The Unix timestamp (in seconds) for when the permission was created.
     */
    @SerialName("created_at")
    val createdAt: Long,
    /**
     * The project identifier that the permission is for.
     */
    @SerialName("project_id")
    val projectId: String,
    /**
     * The object type, which is always "checkpoint.permission".
     */
    @SerialName("object")
    val objectType: String
)
