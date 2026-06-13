// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * URL backing an annotation entry.
 */
@Serializable
data class UrlAnnotationSource(
    /**
     * Type discriminator that is always `url`.
     */
    val type: String = "url",
    /**
     * URL referenced by the annotation.
     */
    val url: String
)
