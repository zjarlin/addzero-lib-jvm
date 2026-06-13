// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai

import de.jensklingenberg.ktorfit.http.*
import site.addzero.api.openai.models.CreateVectorStoreFileBatchRequest
import site.addzero.api.openai.models.CreateVectorStoreFileRequest
import site.addzero.api.openai.models.CreateVectorStoreRequest
import site.addzero.api.openai.models.DeleteVectorStoreFileResponse
import site.addzero.api.openai.models.DeleteVectorStoreResponse
import site.addzero.api.openai.models.ListVectorStoreFilesResponse
import site.addzero.api.openai.models.ListVectorStoresResponse
import site.addzero.api.openai.models.UpdateVectorStoreFileAttributesRequest
import site.addzero.api.openai.models.UpdateVectorStoreRequest
import site.addzero.api.openai.models.VectorStoreFileBatchObject
import site.addzero.api.openai.models.VectorStoreFileContentResponse
import site.addzero.api.openai.models.VectorStoreFileObject
import site.addzero.api.openai.models.VectorStoreObject
import site.addzero.api.openai.models.VectorStoreSearchRequest
import site.addzero.api.openai.models.VectorStoreSearchResultsPage

interface OpenAiVectorStoresApi {

    /**
     * Returns a list of vector stores. REST: GET /vector_stores
     */
    @GET(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.VECTOR_STORES)
    suspend fun listVectorStores(
        @Query("limit") limit: Int? = null,
        @Query("order") order: String? = null,
        @Query("after") after: String? = null,
        @Query("before") before: String? = null
    ): site.addzero.api.openai.models.ListVectorStoresResponse

    /**
     * Create a vector store. REST: POST /vector_stores
     */
    @POST(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.VECTOR_STORES)
    suspend fun createVectorStore(
        @Body body: site.addzero.api.openai.models.CreateVectorStoreRequest
    ): site.addzero.api.openai.models.VectorStoreObject

    /**
     * Retrieves a vector store. REST: GET /vector_stores/{vector_store_id}
     */
    @GET(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.VECTOR_STORES_BY_VECTOR_STORE_ID)
    suspend fun getVectorStore(
        @Path("vector_store_id") vectorStoreId: String
    ): site.addzero.api.openai.models.VectorStoreObject

    /**
     * Modifies a vector store. REST: POST /vector_stores/{vector_store_id}
     */
    @POST(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.VECTOR_STORES_BY_VECTOR_STORE_ID)
    suspend fun modifyVectorStore(
        @Path("vector_store_id") vectorStoreId: String,
        @Body body: site.addzero.api.openai.models.UpdateVectorStoreRequest
    ): site.addzero.api.openai.models.VectorStoreObject

    /**
     * Delete a vector store. REST: DELETE /vector_stores/{vector_store_id}
     */
    @DELETE(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.VECTOR_STORES_BY_VECTOR_STORE_ID)
    suspend fun deleteVectorStore(
        @Path("vector_store_id") vectorStoreId: String
    ): site.addzero.api.openai.models.DeleteVectorStoreResponse

    /**
     * Create a vector store file batch. REST: POST /vector_stores/{vector_store_id}/file_batches
     */
    @POST(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.VECTOR_STORES_BY_VECTOR_STORE_ID_BY_FILE_BATCHES)
    suspend fun createVectorStoreFileBatch(
        @Path("vector_store_id") vectorStoreId: String,
        @Body body: site.addzero.api.openai.models.CreateVectorStoreFileBatchRequest
    ): site.addzero.api.openai.models.VectorStoreFileBatchObject

    /**
     * Retrieves a vector store file batch. REST: GET
     * /vector_stores/{vector_store_id}/file_batches/{batch_id}
     */
    @GET(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.VECTOR_STORES_BY_VECTOR_STORE_ID_BY_FILE_BATCHES_BY_BATCH_ID)
    suspend fun getVectorStoreFileBatch(
        @Path("vector_store_id") vectorStoreId: String,
        @Path("batch_id") batchId: String
    ): site.addzero.api.openai.models.VectorStoreFileBatchObject

    /**
     * Cancel a vector store file batch. This attempts to cancel the processing of files in this batch as
     * soon as possible. REST: POST /vector_stores/{vector_store_id}/file_batches/{batch_id}/cancel
     */
    @POST(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.VECTOR_STORES_BY_VECTOR_STORE_ID_BY_FILE_BATCHES_BY_BATCH_ID_BY_CANCEL)
    suspend fun cancelVectorStoreFileBatch(
        @Path("vector_store_id") vectorStoreId: String,
        @Path("batch_id") batchId: String
    ): site.addzero.api.openai.models.VectorStoreFileBatchObject

    /**
     * Returns a list of vector store files in a batch. REST: GET
     * /vector_stores/{vector_store_id}/file_batches/{batch_id}/files
     */
    @GET(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.VECTOR_STORES_BY_VECTOR_STORE_ID_BY_FILE_BATCHES_BY_BATCH_ID_BY_FILES)
    suspend fun listFilesInVectorStoreBatch(
        @Path("vector_store_id") vectorStoreId: String,
        @Path("batch_id") batchId: String,
        @Query("limit") limit: Int? = null,
        @Query("order") order: String? = null,
        @Query("after") after: String? = null,
        @Query("before") before: String? = null,
        @Query("filter") filter: String? = null
    ): site.addzero.api.openai.models.ListVectorStoreFilesResponse

    /**
     * Returns a list of vector store files. REST: GET /vector_stores/{vector_store_id}/files
     */
    @GET(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.VECTOR_STORES_BY_VECTOR_STORE_ID_BY_FILES)
    suspend fun listVectorStoreFiles(
        @Path("vector_store_id") vectorStoreId: String,
        @Query("limit") limit: Int? = null,
        @Query("order") order: String? = null,
        @Query("after") after: String? = null,
        @Query("before") before: String? = null,
        @Query("filter") filter: String? = null
    ): site.addzero.api.openai.models.ListVectorStoreFilesResponse

    /**
     * Create a vector store file by attaching a [File](/docs/api-reference/files) to a [vector
     * store](/docs/api-reference/vector-stores/object). REST: POST /vector_stores/{vector_store_id}/files
     */
    @POST(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.VECTOR_STORES_BY_VECTOR_STORE_ID_BY_FILES)
    suspend fun createVectorStoreFile(
        @Path("vector_store_id") vectorStoreId: String,
        @Body body: site.addzero.api.openai.models.CreateVectorStoreFileRequest
    ): site.addzero.api.openai.models.VectorStoreFileObject

    /**
     * Retrieves a vector store file. REST: GET /vector_stores/{vector_store_id}/files/{file_id}
     */
    @GET(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.VECTOR_STORES_BY_VECTOR_STORE_ID_BY_FILES_BY_FILE_ID)
    suspend fun getVectorStoreFile(
        @Path("vector_store_id") vectorStoreId: String,
        @Path("file_id") fileId: String
    ): site.addzero.api.openai.models.VectorStoreFileObject

    /**
     * Update attributes on a vector store file. REST: POST
     * /vector_stores/{vector_store_id}/files/{file_id}
     */
    @POST(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.VECTOR_STORES_BY_VECTOR_STORE_ID_BY_FILES_BY_FILE_ID)
    suspend fun updateVectorStoreFileAttributes(
        @Path("vector_store_id") vectorStoreId: String,
        @Path("file_id") fileId: String,
        @Body body: site.addzero.api.openai.models.UpdateVectorStoreFileAttributesRequest
    ): site.addzero.api.openai.models.VectorStoreFileObject

    /**
     * Delete a vector store file. This will remove the file from the vector store but the file itself will
     * not be deleted. To delete the file, use the [delete file](/docs/api-reference/files/delete)
     * endpoint. REST: DELETE /vector_stores/{vector_store_id}/files/{file_id}
     */
    @DELETE(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.VECTOR_STORES_BY_VECTOR_STORE_ID_BY_FILES_BY_FILE_ID)
    suspend fun deleteVectorStoreFile(
        @Path("vector_store_id") vectorStoreId: String,
        @Path("file_id") fileId: String
    ): site.addzero.api.openai.models.DeleteVectorStoreFileResponse

    /**
     * Retrieve the parsed contents of a vector store file. REST: GET
     * /vector_stores/{vector_store_id}/files/{file_id}/content
     */
    @GET(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.VECTOR_STORES_BY_VECTOR_STORE_ID_BY_FILES_BY_FILE_ID_BY_CONTENT)
    suspend fun retrieveVectorStoreFileContent(
        @Path("vector_store_id") vectorStoreId: String,
        @Path("file_id") fileId: String
    ): site.addzero.api.openai.models.VectorStoreFileContentResponse

    /**
     * Search a vector store for relevant chunks based on a query and file attributes filter. REST: POST
     * /vector_stores/{vector_store_id}/search
     */
    @POST(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.VECTOR_STORES_BY_VECTOR_STORE_ID_BY_SEARCH)
    suspend fun searchVectorStore(
        @Path("vector_store_id") vectorStoreId: String,
        @Body body: site.addzero.api.openai.models.VectorStoreSearchRequest
    ): site.addzero.api.openai.models.VectorStoreSearchResultsPage

}
