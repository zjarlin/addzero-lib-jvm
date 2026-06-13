// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * Forces the model to call the apply_patch tool when executing a tool call.
 */
@Serializable
data class SpecificApplyPatchParam(
    /**
     * The tool to call. Always `apply_patch`.
     */
    val type: String = "apply_patch"
)
