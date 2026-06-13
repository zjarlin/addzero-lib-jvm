// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents an embedding vector returned by embedding endpoint.
 */
@Serializable
data class Embedding(
    /**
     * The index of the embedding in the list of embeddings.
     */
    val index: Int,
    /**
     * The embedding vector, which is a list of floats. The length of vector depends on the model as listed
     * in the [embedding guide](/docs/guides/embeddings).
     */
    val embedding: List<Float>,
    /**
     * The object type, which is always "embedding".
     */
    @SerialName("object")
    val objectType: String
)
