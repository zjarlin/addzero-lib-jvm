// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * Your chosen grammar.
 */
@Serializable
data class CustomToolChatCompletionsCustomFormat2Grammar(
    /**
     * The grammar definition.
     */
    val definition: String,
    /**
     * The syntax of the grammar definition. One of `lark` or `regex`.
     */
    val syntax: String
)
