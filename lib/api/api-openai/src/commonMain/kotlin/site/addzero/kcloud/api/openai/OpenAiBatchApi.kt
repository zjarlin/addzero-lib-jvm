// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.kcloud.api.openai

import de.jensklingenberg.ktorfit.http.*

/** Batch REST endpoints. */
interface OpenAiBatchApi {

    /**
     * List your organization's batches.
     *
     * REST: GET /batches
     */
    @GET(OpenAiApiPaths.BATCHES)
    suspend fun listBatches(
        @Query("after") after: String? = null,
        @Query("limit") limit: Int? = null
    ): OpenAiResponseBody

    /**
     * Creates and executes a batch from an uploaded file of requests
     *
     * REST: POST /batches
     */
    @POST(OpenAiApiPaths.BATCHES)
    suspend fun createBatch(
        @Body body: OpenAiRequestBody
    ): OpenAiResponseBody

    /**
     * Retrieves a batch.
     *
     * REST: GET /batches/{batch_id}
     */
    @GET(OpenAiApiPaths.BATCHES_BY_BATCH_ID)
    suspend fun retrieveBatch(
        @Path("batch_id") batchId: String
    ): OpenAiResponseBody

    /**
     * Cancels an in-progress batch. The batch will be in status `cancelling` for up to 10 minutes, before changing to `cancelled`, where it will have partial results (if any) available in the output file.
     *
     * REST: POST /batches/{batch_id}/cancel
     */
    @POST(OpenAiApiPaths.BATCHES_BY_BATCH_ID_BY_CANCEL)
    suspend fun cancelBatch(
        @Path("batch_id") batchId: String
    ): OpenAiResponseBody
}
