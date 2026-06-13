// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * Instruction describing how to delete a file via the apply_patch tool.
 */
@Serializable
data class ApplyPatchDeleteFileOperation(
    /**
     * Delete the specified file.
     */
    val type: String = "delete_file",
    /**
     * Path of the file to delete.
     */
    val path: String
)
