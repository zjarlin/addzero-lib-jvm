// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * Instruction for updating an existing file via the apply_patch tool.
 */
@Serializable
data class ApplyPatchUpdateFileOperationParam(
    /**
     * The operation type. Always `update_file`.
     */
    val type: String = "update_file",
    /**
     * Path of the file to update relative to the workspace root.
     */
    val path: String,
    /**
     * Unified diff content to apply to the existing file.
     */
    val diff: String
)
