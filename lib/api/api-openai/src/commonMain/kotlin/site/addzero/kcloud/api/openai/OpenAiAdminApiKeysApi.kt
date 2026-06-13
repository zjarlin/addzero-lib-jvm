// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.kcloud.api.openai

import de.jensklingenberg.ktorfit.http.*

/** Admin API keys REST endpoints. */
interface OpenAiAdminApiKeysApi {

    /**
     * List organization API keys
     *
     * REST: GET /organization/admin_api_keys
     */
    @GET(OpenAiApiPaths.ORGANIZATION_BY_ADMIN_API_KEYS)
    suspend fun adminApiKeysList(
        @Query("after") after: String? = null,
        @Query("order") order: String? = null,
        @Query("limit") limit: Int? = null
    ): OpenAiResponseBody

    /**
     * Create an organization admin API key
     *
     * REST: POST /organization/admin_api_keys
     */
    @POST(OpenAiApiPaths.ORGANIZATION_BY_ADMIN_API_KEYS)
    suspend fun adminApiKeysCreate(
        @Body body: OpenAiRequestBody
    ): OpenAiResponseBody

    /**
     * Delete an organization admin API key
     *
     * REST: DELETE /organization/admin_api_keys/{key_id}
     */
    @DELETE(OpenAiApiPaths.ORGANIZATION_BY_ADMIN_API_KEYS_BY_KEY_ID)
    suspend fun adminApiKeysDelete(
        @Path("key_id") keyId: String
    ): OpenAiResponseBody

    /**
     * Retrieve a single organization API key
     *
     * REST: GET /organization/admin_api_keys/{key_id}
     */
    @GET(OpenAiApiPaths.ORGANIZATION_BY_ADMIN_API_KEYS_BY_KEY_ID)
    suspend fun adminApiKeysGet(
        @Path("key_id") keyId: String
    ): OpenAiResponseBody
}
