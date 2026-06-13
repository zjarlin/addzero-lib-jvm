// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai

import de.jensklingenberg.ktorfit.http.*
import site.addzero.api.openai.models.Batch
import site.addzero.api.openai.models.CreateBatchRequest
import site.addzero.api.openai.models.ListBatchesResponse

interface OpenAiBatchApi {

    /**
     * List your organization's batches. REST: GET /batches
     */
    @GET(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.BATCHES)
    suspend fun listBatches(
        @Query("after") after: String? = null,
        @Query("limit") limit: Int? = null
    ): site.addzero.api.openai.models.ListBatchesResponse

    /**
     * Creates and executes a batch from an uploaded file of requests REST: POST /batches
     */
    @POST(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.BATCHES)
    suspend fun createBatch(
        @Body body: site.addzero.api.openai.models.CreateBatchRequest
    ): site.addzero.api.openai.models.Batch

    /**
     * Retrieves a batch. REST: GET /batches/{batch_id}
     */
    @GET(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.BATCHES_BY_BATCH_ID)
    suspend fun retrieveBatch(
        @Path("batch_id") batchId: String
    ): site.addzero.api.openai.models.Batch

    /**
     * Cancels an in-progress batch. The batch will be in status `cancelling` for up to 10 minutes, before
     * changing to `cancelled`, where it will have partial results (if any) available in the output file.
     * REST: POST /batches/{batch_id}/cancel
     */
    @POST(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.BATCHES_BY_BATCH_ID_BY_CANCEL)
    suspend fun cancelBatch(
        @Path("batch_id") batchId: String
    ): site.addzero.api.openai.models.Batch

}
