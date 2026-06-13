// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.kcloud.api.openai

import de.jensklingenberg.ktorfit.http.*

/** Vector stores REST endpoints. */
interface OpenAiVectorStoresApi {

    /**
     * Returns a list of vector stores.
     *
     * REST: GET /vector_stores
     */
    @GET(OpenAiApiPaths.VECTOR_STORES)
    suspend fun listVectorStores(
        @Query("limit") limit: Int? = null,
        @Query("order") order: String? = null,
        @Query("after") after: String? = null,
        @Query("before") before: String? = null
    ): OpenAiResponseBody

    /**
     * Create a vector store.
     *
     * REST: POST /vector_stores
     */
    @POST(OpenAiApiPaths.VECTOR_STORES)
    suspend fun createVectorStore(
        @Body body: OpenAiRequestBody
    ): OpenAiResponseBody

    /**
     * Delete a vector store.
     *
     * REST: DELETE /vector_stores/{vector_store_id}
     */
    @DELETE(OpenAiApiPaths.VECTOR_STORES_BY_VECTOR_STORE_ID)
    suspend fun deleteVectorStore(
        @Path("vector_store_id") vectorStoreId: String
    ): OpenAiResponseBody

    /**
     * Retrieves a vector store.
     *
     * REST: GET /vector_stores/{vector_store_id}
     */
    @GET(OpenAiApiPaths.VECTOR_STORES_BY_VECTOR_STORE_ID)
    suspend fun getVectorStore(
        @Path("vector_store_id") vectorStoreId: String
    ): OpenAiResponseBody

    /**
     * Modifies a vector store.
     *
     * REST: POST /vector_stores/{vector_store_id}
     */
    @POST(OpenAiApiPaths.VECTOR_STORES_BY_VECTOR_STORE_ID)
    suspend fun modifyVectorStore(
        @Path("vector_store_id") vectorStoreId: String,
        @Body body: OpenAiRequestBody
    ): OpenAiResponseBody

    /**
     * Create a vector store file batch.
     *
     * REST: POST /vector_stores/{vector_store_id}/file_batches
     */
    @POST(OpenAiApiPaths.VECTOR_STORES_BY_VECTOR_STORE_ID_BY_FILE_BATCHES)
    suspend fun createVectorStoreFileBatch(
        @Path("vector_store_id") vectorStoreId: String,
        @Body body: OpenAiRequestBody
    ): OpenAiResponseBody

    /**
     * Retrieves a vector store file batch.
     *
     * REST: GET /vector_stores/{vector_store_id}/file_batches/{batch_id}
     */
    @GET(OpenAiApiPaths.VECTOR_STORES_BY_VECTOR_STORE_ID_BY_FILE_BATCHES_BY_BATCH_ID)
    suspend fun getVectorStoreFileBatch(
        @Path("vector_store_id") vectorStoreId: String,
        @Path("batch_id") batchId: String
    ): OpenAiResponseBody

    /**
     * Cancel a vector store file batch. This attempts to cancel the processing of files in this batch as soon as possible.
     *
     * REST: POST /vector_stores/{vector_store_id}/file_batches/{batch_id}/cancel
     */
    @POST(OpenAiApiPaths.VECTOR_STORES_BY_VECTOR_STORE_ID_BY_FILE_BATCHES_BY_BATCH_ID_BY_CANCEL)
    suspend fun cancelVectorStoreFileBatch(
        @Path("vector_store_id") vectorStoreId: String,
        @Path("batch_id") batchId: String
    ): OpenAiResponseBody

    /**
     * Returns a list of vector store files in a batch.
     *
     * REST: GET /vector_stores/{vector_store_id}/file_batches/{batch_id}/files
     */
    @GET(OpenAiApiPaths.VECTOR_STORES_BY_VECTOR_STORE_ID_BY_FILE_BATCHES_BY_BATCH_ID_BY_FILES)
    suspend fun listFilesInVectorStoreBatch(
        @Path("vector_store_id") vectorStoreId: String,
        @Path("batch_id") batchId: String,
        @Query("limit") limit: Int? = null,
        @Query("order") order: String? = null,
        @Query("after") after: String? = null,
        @Query("before") before: String? = null,
        @Query("filter") filter: String? = null
    ): OpenAiResponseBody

    /**
     * Returns a list of vector store files.
     *
     * REST: GET /vector_stores/{vector_store_id}/files
     */
    @GET(OpenAiApiPaths.VECTOR_STORES_BY_VECTOR_STORE_ID_BY_FILES)
    suspend fun listVectorStoreFiles(
        @Path("vector_store_id") vectorStoreId: String,
        @Query("limit") limit: Int? = null,
        @Query("order") order: String? = null,
        @Query("after") after: String? = null,
        @Query("before") before: String? = null,
        @Query("filter") filter: String? = null
    ): OpenAiResponseBody

    /**
     * Create a vector store file by attaching a [File](/docs/api-reference/files) to a [vector store](/docs/api-reference/vector-stores/object).
     *
     * REST: POST /vector_stores/{vector_store_id}/files
     */
    @POST(OpenAiApiPaths.VECTOR_STORES_BY_VECTOR_STORE_ID_BY_FILES)
    suspend fun createVectorStoreFile(
        @Path("vector_store_id") vectorStoreId: String,
        @Body body: OpenAiRequestBody
    ): OpenAiResponseBody

    /**
     * Delete a vector store file. This will remove the file from the vector store but the file itself will not be deleted. To delete the file, use the [delete file](/docs/api-reference/files/delete) endpoint.
     *
     * REST: DELETE /vector_stores/{vector_store_id}/files/{file_id}
     */
    @DELETE(OpenAiApiPaths.VECTOR_STORES_BY_VECTOR_STORE_ID_BY_FILES_BY_FILE_ID)
    suspend fun deleteVectorStoreFile(
        @Path("vector_store_id") vectorStoreId: String,
        @Path("file_id") fileId: String
    ): OpenAiResponseBody

    /**
     * Retrieves a vector store file.
     *
     * REST: GET /vector_stores/{vector_store_id}/files/{file_id}
     */
    @GET(OpenAiApiPaths.VECTOR_STORES_BY_VECTOR_STORE_ID_BY_FILES_BY_FILE_ID)
    suspend fun getVectorStoreFile(
        @Path("vector_store_id") vectorStoreId: String,
        @Path("file_id") fileId: String
    ): OpenAiResponseBody

    /**
     * Update attributes on a vector store file.
     *
     * REST: POST /vector_stores/{vector_store_id}/files/{file_id}
     */
    @POST(OpenAiApiPaths.VECTOR_STORES_BY_VECTOR_STORE_ID_BY_FILES_BY_FILE_ID)
    suspend fun updateVectorStoreFileAttributes(
        @Path("vector_store_id") vectorStoreId: String,
        @Path("file_id") fileId: String,
        @Body body: OpenAiRequestBody
    ): OpenAiResponseBody

    /**
     * Retrieve the parsed contents of a vector store file.
     *
     * REST: GET /vector_stores/{vector_store_id}/files/{file_id}/content
     */
    @GET(OpenAiApiPaths.VECTOR_STORES_BY_VECTOR_STORE_ID_BY_FILES_BY_FILE_ID_BY_CONTENT)
    suspend fun retrieveVectorStoreFileContent(
        @Path("vector_store_id") vectorStoreId: String,
        @Path("file_id") fileId: String
    ): OpenAiBinaryBody

    /**
     * Search a vector store for relevant chunks based on a query and file attributes filter.
     *
     * REST: POST /vector_stores/{vector_store_id}/search
     */
    @POST(OpenAiApiPaths.VECTOR_STORES_BY_VECTOR_STORE_ID_BY_SEARCH)
    suspend fun searchVectorStore(
        @Path("vector_store_id") vectorStoreId: String,
        @Body body: OpenAiRequestBody
    ): OpenAiResponseBody
}
