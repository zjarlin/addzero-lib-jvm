// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * A StringCheckGrader object that performs a string comparison between input and reference using a
 * specified operation.
 */
@Serializable
data class GraderStringCheck(
    /**
     * The object type, which is always `string_check`.
     */
    val type: String,
    /**
     * The name of the grader.
     */
    val name: String,
    /**
     * The input text. This may include template strings.
     */
    val input: String,
    /**
     * The reference text. This may include template strings.
     */
    val reference: String,
    /**
     * The string check operation to perform. One of `eq`, `ne`, `like`, or `ilike`.
     */
    val operation: String
)
