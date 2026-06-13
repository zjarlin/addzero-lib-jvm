// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * Allows the assistant to create, delete, or update files using unified diffs.
 */
@Serializable
data class ApplyPatchToolParam(
    /**
     * The type of the tool. Always `apply_patch`.
     */
    val type: String = "apply_patch"
)
