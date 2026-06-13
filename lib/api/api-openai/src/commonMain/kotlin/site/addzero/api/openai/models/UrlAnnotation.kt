// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * Annotation that references a URL.
 */
@Serializable
data class UrlAnnotation(
    /**
     * Type discriminator that is always `url` for this annotation.
     */
    val type: String = "url",
    /**
     * URL referenced by the annotation.
     */
    val source: site.addzero.api.openai.models.UrlAnnotationSource
)
