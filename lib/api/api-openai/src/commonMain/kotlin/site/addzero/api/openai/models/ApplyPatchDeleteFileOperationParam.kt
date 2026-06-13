// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * Instruction for deleting an existing file via the apply_patch tool.
 */
@Serializable
data class ApplyPatchDeleteFileOperationParam(
    /**
     * The operation type. Always `delete_file`.
     */
    val type: String = "delete_file",
    /**
     * Path of the file to delete relative to the workspace root.
     */
    val path: String
)
