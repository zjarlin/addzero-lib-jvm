// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DeleteFineTuningCheckpointPermissionResponse(
    /**
     * The ID of the fine-tuned model checkpoint permission that was deleted.
     */
    val id: String,
    /**
     * The object type, which is always "checkpoint.permission".
     */
    @SerialName("object")
    val objectType: String,
    /**
     * Whether the fine-tuned model checkpoint permission was successfully deleted.
     */
    val deleted: Boolean
)
