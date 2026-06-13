// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * Instruction for creating a new file via the apply_patch tool.
 */
@Serializable
data class ApplyPatchCreateFileOperationParam(
    /**
     * The operation type. Always `create_file`.
     */
    val type: String = "create_file",
    /**
     * Path of the file to create relative to the workspace root.
     */
    val path: String,
    /**
     * Unified diff content to apply when creating the file.
     */
    val diff: String
)
