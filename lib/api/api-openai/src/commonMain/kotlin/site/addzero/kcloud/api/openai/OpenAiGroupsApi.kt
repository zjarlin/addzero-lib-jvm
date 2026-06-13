// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.kcloud.api.openai

import de.jensklingenberg.ktorfit.http.*

/** Groups REST endpoints. */
interface OpenAiGroupsApi {

    /**
     * Lists all groups in the organization.
     *
     * REST: GET /organization/groups
     */
    @GET(OpenAiApiPaths.ORGANIZATION_BY_GROUPS)
    suspend fun listGroups(
        @Query("limit") limit: Int? = null,
        @Query("after") after: String? = null,
        @Query("order") order: String? = null
    ): OpenAiResponseBody

    /**
     * Creates a new group in the organization.
     *
     * REST: POST /organization/groups
     */
    @POST(OpenAiApiPaths.ORGANIZATION_BY_GROUPS)
    suspend fun createGroup(
        @Body body: OpenAiRequestBody
    ): OpenAiResponseBody

    /**
     * Deletes a group from the organization.
     *
     * REST: DELETE /organization/groups/{group_id}
     */
    @DELETE(OpenAiApiPaths.ORGANIZATION_BY_GROUPS_BY_GROUP_ID)
    suspend fun deleteGroup(
        @Path("group_id") groupId: String
    ): OpenAiResponseBody

    /**
     * Updates a group's information.
     *
     * REST: POST /organization/groups/{group_id}
     */
    @POST(OpenAiApiPaths.ORGANIZATION_BY_GROUPS_BY_GROUP_ID)
    suspend fun updateGroup(
        @Path("group_id") groupId: String,
        @Body body: OpenAiRequestBody
    ): OpenAiResponseBody
}
