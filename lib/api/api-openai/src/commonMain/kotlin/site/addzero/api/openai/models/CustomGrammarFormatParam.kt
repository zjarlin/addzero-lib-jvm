// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * A grammar defined by the user.
 */
@Serializable
data class CustomGrammarFormatParam(
    /**
     * Grammar format. Always `grammar`.
     */
    val type: String = "grammar",
    /**
     * The syntax of the grammar definition. One of `lark` or `regex`.
     */
    val syntax: site.addzero.api.openai.models.GrammarSyntax1,
    /**
     * The grammar definition.
     */
    val definition: String
)
