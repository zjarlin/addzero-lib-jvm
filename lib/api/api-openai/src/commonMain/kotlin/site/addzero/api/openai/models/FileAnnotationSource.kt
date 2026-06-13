// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * Attachment source referenced by an annotation.
 */
@Serializable
data class FileAnnotationSource(
    /**
     * Type discriminator that is always `file`.
     */
    val type: String = "file",
    /**
     * Filename referenced by the annotation.
     */
    val filename: String
)
