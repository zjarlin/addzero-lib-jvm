// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai

import de.jensklingenberg.ktorfit.http.*
import site.addzero.api.openai.models.AdminApiKey
import site.addzero.api.openai.models.AdminApiKeyCreateResponse
import site.addzero.api.openai.models.AdminApiKeysCreateRequest
import site.addzero.api.openai.models.AdminApiKeysDeleteResponse
import site.addzero.api.openai.models.ApiKeyList

interface OpenAiAdminApiKeysApi {

    /**
     * List organization API keys REST: GET /organization/admin_api_keys
     */
    @GET(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.ORGANIZATION_BY_ADMIN_API_KEYS)
    suspend fun adminApiKeysList(
        @Query("after") after: String? = null,
        @Query("order") order: String? = null,
        @Query("limit") limit: Int? = null
    ): site.addzero.api.openai.models.ApiKeyList

    /**
     * Create an organization admin API key REST: POST /organization/admin_api_keys
     */
    @POST(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.ORGANIZATION_BY_ADMIN_API_KEYS)
    suspend fun adminApiKeysCreate(
        @Body body: site.addzero.api.openai.models.AdminApiKeysCreateRequest
    ): site.addzero.api.openai.models.AdminApiKeyCreateResponse

    /**
     * Retrieve a single organization API key REST: GET /organization/admin_api_keys/{key_id}
     */
    @GET(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.ORGANIZATION_BY_ADMIN_API_KEYS_BY_KEY_ID)
    suspend fun adminApiKeysGet(
        @Path("key_id") keyId: String
    ): site.addzero.api.openai.models.AdminApiKey

    /**
     * Delete an organization admin API key REST: DELETE /organization/admin_api_keys/{key_id}
     */
    @DELETE(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.ORGANIZATION_BY_ADMIN_API_KEYS_BY_KEY_ID)
    suspend fun adminApiKeysDelete(
        @Path("key_id") keyId: String
    ): site.addzero.api.openai.models.AdminApiKeysDeleteResponse

}
