// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai

import de.jensklingenberg.ktorfit.http.*
import site.addzero.api.openai.models.User
import site.addzero.api.openai.models.UserDeleteResponse
import site.addzero.api.openai.models.UserListResponse
import site.addzero.api.openai.models.UserRoleUpdateRequest

interface OpenAiUsersApi {

    /**
     * Lists all of the users in the organization. REST: GET /organization/users
     */
    @GET(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.ORGANIZATION_BY_USERS)
    suspend fun listUsers(
        @Query("limit") limit: Int? = null,
        @Query("after") after: String? = null,
        @Query("emails") emails: List<String>? = null
    ): site.addzero.api.openai.models.UserListResponse

    /**
     * Retrieves a user by their identifier. REST: GET /organization/users/{user_id}
     */
    @GET(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.ORGANIZATION_BY_USERS_BY_USER_ID)
    suspend fun retrieveUser(
        @Path("user_id") userId: String
    ): site.addzero.api.openai.models.User

    /**
     * Modifies a user's role in the organization. REST: POST /organization/users/{user_id}
     */
    @POST(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.ORGANIZATION_BY_USERS_BY_USER_ID)
    suspend fun modifyUser(
        @Path("user_id") userId: String,
        @Body body: site.addzero.api.openai.models.UserRoleUpdateRequest
    ): site.addzero.api.openai.models.User

    /**
     * Deletes a user from the organization. REST: DELETE /organization/users/{user_id}
     */
    @DELETE(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.ORGANIZATION_BY_USERS_BY_USER_ID)
    suspend fun deleteUser(
        @Path("user_id") userId: String
    ): site.addzero.api.openai.models.UserDeleteResponse

}
