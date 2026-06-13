// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * Instruction describing how to update a file via the apply_patch tool.
 */
@Serializable
data class ApplyPatchUpdateFileOperation(
    /**
     * Update an existing file with the provided diff.
     */
    val type: String = "update_file",
    /**
     * Path of the file to update.
     */
    val path: String,
    /**
     * Diff to apply.
     */
    val diff: String
)
