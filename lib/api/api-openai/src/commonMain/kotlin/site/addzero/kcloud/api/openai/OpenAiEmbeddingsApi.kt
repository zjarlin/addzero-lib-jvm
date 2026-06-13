// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.kcloud.api.openai

import de.jensklingenberg.ktorfit.http.*

/** Embeddings REST endpoints. */
interface OpenAiEmbeddingsApi {

    /**
     * Creates an embedding vector representing the input text.
     *
     * REST: POST /embeddings
     */
    @POST(OpenAiApiPaths.EMBEDDINGS)
    suspend fun createEmbedding(
        @Body body: OpenAiRequestBody
    ): OpenAiResponseBody
}
