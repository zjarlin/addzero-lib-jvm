// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * Reference to a prompt template and its variables. [Learn more](/docs/guides/text?api-
 * mode=responses#reusable-prompts).
 */
@Serializable
data class Prompt2(
    /**
     * The unique identifier of the prompt template to use.
     */
    val id: String,
    val version: String? = null,
    val variables: site.addzero.api.openai.models.ResponsePromptVariables? = null
)
