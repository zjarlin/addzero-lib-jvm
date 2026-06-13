// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * Instruction describing how to create a file via the apply_patch tool.
 */
@Serializable
data class ApplyPatchCreateFileOperation(
    /**
     * Create a new file with the provided diff.
     */
    val type: String = "create_file",
    /**
     * Path of the file to create.
     */
    val path: String,
    /**
     * Diff to apply.
     */
    val diff: String
)
