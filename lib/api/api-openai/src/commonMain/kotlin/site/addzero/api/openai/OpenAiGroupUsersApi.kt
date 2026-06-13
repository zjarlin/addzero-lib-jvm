// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai

import de.jensklingenberg.ktorfit.http.*
import site.addzero.api.openai.models.CreateGroupUserBody
import site.addzero.api.openai.models.GroupUserAssignment
import site.addzero.api.openai.models.GroupUserDeletedResource
import site.addzero.api.openai.models.UserListResource

interface OpenAiGroupUsersApi {

    /**
     * Lists the users assigned to a group. REST: GET /organization/groups/{group_id}/users
     */
    @GET(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.ORGANIZATION_BY_GROUPS_BY_GROUP_ID_BY_USERS)
    suspend fun listGroupUsers(
        @Path("group_id") groupId: String,
        @Query("limit") limit: Int? = null,
        @Query("after") after: String? = null,
        @Query("order") order: String? = null
    ): site.addzero.api.openai.models.UserListResource

    /**
     * Adds a user to a group. REST: POST /organization/groups/{group_id}/users
     */
    @POST(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.ORGANIZATION_BY_GROUPS_BY_GROUP_ID_BY_USERS)
    suspend fun addGroupUser(
        @Path("group_id") groupId: String,
        @Body body: site.addzero.api.openai.models.CreateGroupUserBody
    ): site.addzero.api.openai.models.GroupUserAssignment

    /**
     * Removes a user from a group. REST: DELETE /organization/groups/{group_id}/users/{user_id}
     */
    @DELETE(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.ORGANIZATION_BY_GROUPS_BY_GROUP_ID_BY_USERS_BY_USER_ID)
    suspend fun removeGroupUser(
        @Path("group_id") groupId: String,
        @Path("user_id") userId: String
    ): site.addzero.api.openai.models.GroupUserDeletedResource

}
