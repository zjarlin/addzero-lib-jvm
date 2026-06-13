// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * The image output from the code interpreter.
 */
@Serializable
data class CodeInterpreterOutputImage(
    /**
     * The type of the output. Always `image`.
     */
    val type: String = "image",
    /**
     * The URL of the image output from the code interpreter.
     */
    val url: String
)
