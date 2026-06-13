// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

@Serializable
data class AddUploadPartRequest(
    /**
     * The chunk of bytes for this Part.
     */
    val data: ByteArray
)
