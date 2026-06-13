// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * Indicates that the model should use a built-in tool to generate a response. [Learn more about built-
 * in tools](/docs/guides/tools).
 */
@Serializable
data class ToolChoiceTypes(
    /**
     * The type of hosted tool the model should to use. Learn more about [built-in
     * tools](/docs/guides/tools). Allowed values are: - `file_search` - `web_search_preview` - `computer`
     * - `computer_use_preview` - `computer_use` - `code_interpreter` - `image_generation`
     */
    val type: String
)
