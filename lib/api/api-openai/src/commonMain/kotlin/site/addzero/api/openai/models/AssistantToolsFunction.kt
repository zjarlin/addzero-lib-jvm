// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * Function tool
 */
@Serializable
data class AssistantToolsFunction(
    /**
     * The type of tool being defined: `function`
     */
    val type: String,
    val function: site.addzero.api.openai.models.FunctionObject
)
