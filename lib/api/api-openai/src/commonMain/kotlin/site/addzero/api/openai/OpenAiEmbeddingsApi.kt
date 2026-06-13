// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai

import de.jensklingenberg.ktorfit.http.*
import site.addzero.api.openai.models.CreateEmbeddingRequest
import site.addzero.api.openai.models.CreateEmbeddingResponse

interface OpenAiEmbeddingsApi {

    /**
     * Creates an embedding vector representing the input text. REST: POST /embeddings
     */
    @POST(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.EMBEDDINGS)
    suspend fun createEmbedding(
        @Body body: site.addzero.api.openai.models.CreateEmbeddingRequest
    ): site.addzero.api.openai.models.CreateEmbeddingResponse

}
