// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * Code interpreter tool
 */
@Serializable
data class AssistantToolsCode(
    /**
     * The type of tool being defined: `code_interpreter`
     */
    val type: String
)
