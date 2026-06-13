// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * Annotation that references an uploaded file.
 */
@Serializable
data class FileAnnotation(
    /**
     * Type discriminator that is always `file` for this annotation.
     */
    val type: String = "file",
    /**
     * File attachment referenced by the annotation.
     */
    val source: site.addzero.api.openai.models.FileAnnotationSource
)
