// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * The last error associated with this vector store file. Will be `null` if there are no errors.
 */
@Serializable
data class VectorStoreFileObjectLastError(
    /**
     * One of `server_error`, `unsupported_file`, or `invalid_file`.
     */
    val code: String,
    /**
     * A human-readable description of the error.
     */
    val message: String
)
