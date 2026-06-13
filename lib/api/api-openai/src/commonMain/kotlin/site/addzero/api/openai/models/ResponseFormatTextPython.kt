// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * Configure the model to generate valid Python code. See the [custom grammars
 * guide](/docs/guides/custom-grammars) for more details.
 */
@Serializable
data class ResponseFormatTextPython(
    /**
     * The type of response format being defined. Always `python`.
     */
    val type: String
)
