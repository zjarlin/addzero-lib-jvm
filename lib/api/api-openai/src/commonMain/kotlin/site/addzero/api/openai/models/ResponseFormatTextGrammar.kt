// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * A custom grammar for the model to follow when generating text. Learn more in the [custom grammars
 * guide](/docs/guides/custom-grammars).
 */
@Serializable
data class ResponseFormatTextGrammar(
    /**
     * The type of response format being defined. Always `grammar`.
     */
    val type: String,
    /**
     * The custom grammar for the model to follow.
     */
    val grammar: String
)
