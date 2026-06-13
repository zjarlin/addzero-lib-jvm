// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.kcloud.api.openai

import de.jensklingenberg.ktorfit.http.*

/** Users REST endpoints. */
interface OpenAiUsersApi {

    /**
     * Lists all of the users in the organization.
     *
     * REST: GET /organization/users
     */
    @GET(OpenAiApiPaths.ORGANIZATION_BY_USERS)
    suspend fun listUsers(
        @Query("limit") limit: Int? = null,
        @Query("after") after: String? = null,
        @Query("emails") emails: List<String>? = null
    ): OpenAiResponseBody

    /**
     * Deletes a user from the organization.
     *
     * REST: DELETE /organization/users/{user_id}
     */
    @DELETE(OpenAiApiPaths.ORGANIZATION_BY_USERS_BY_USER_ID)
    suspend fun deleteUser(
        @Path("user_id") userId: String
    ): OpenAiResponseBody

    /**
     * Retrieves a user by their identifier.
     *
     * REST: GET /organization/users/{user_id}
     */
    @GET(OpenAiApiPaths.ORGANIZATION_BY_USERS_BY_USER_ID)
    suspend fun retrieveUser(
        @Path("user_id") userId: String
    ): OpenAiResponseBody

    /**
     * Modifies a user's role in the organization.
     *
     * REST: POST /organization/users/{user_id}
     */
    @POST(OpenAiApiPaths.ORGANIZATION_BY_USERS_BY_USER_ID)
    suspend fun modifyUser(
        @Path("user_id") userId: String,
        @Body body: OpenAiRequestBody
    ): OpenAiResponseBody
}
