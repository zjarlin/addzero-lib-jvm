// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.kcloud.api.openai

import de.jensklingenberg.ktorfit.http.*

/** Group users REST endpoints. */
interface OpenAiGroupUsersApi {

    /**
     * Lists the users assigned to a group.
     *
     * REST: GET /organization/groups/{group_id}/users
     */
    @GET(OpenAiApiPaths.ORGANIZATION_BY_GROUPS_BY_GROUP_ID_BY_USERS)
    suspend fun listGroupUsers(
        @Path("group_id") groupId: String,
        @Query("limit") limit: Int? = null,
        @Query("after") after: String? = null,
        @Query("order") order: String? = null
    ): OpenAiResponseBody

    /**
     * Adds a user to a group.
     *
     * REST: POST /organization/groups/{group_id}/users
     */
    @POST(OpenAiApiPaths.ORGANIZATION_BY_GROUPS_BY_GROUP_ID_BY_USERS)
    suspend fun addGroupUser(
        @Path("group_id") groupId: String,
        @Body body: OpenAiRequestBody
    ): OpenAiResponseBody

    /**
     * Removes a user from a group.
     *
     * REST: DELETE /organization/groups/{group_id}/users/{user_id}
     */
    @DELETE(OpenAiApiPaths.ORGANIZATION_BY_GROUPS_BY_GROUP_ID_BY_USERS_BY_USER_ID)
    suspend fun removeGroupUser(
        @Path("group_id") groupId: String,
        @Path("user_id") userId: String
    ): OpenAiResponseBody
}
