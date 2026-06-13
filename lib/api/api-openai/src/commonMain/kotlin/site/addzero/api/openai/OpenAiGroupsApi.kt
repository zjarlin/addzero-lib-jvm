// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai

import de.jensklingenberg.ktorfit.http.*
import site.addzero.api.openai.models.CreateGroupBody
import site.addzero.api.openai.models.GroupDeletedResource
import site.addzero.api.openai.models.GroupListResource
import site.addzero.api.openai.models.GroupResourceWithSuccess
import site.addzero.api.openai.models.GroupResponse
import site.addzero.api.openai.models.UpdateGroupBody

interface OpenAiGroupsApi {

    /**
     * Lists all groups in the organization. REST: GET /organization/groups
     */
    @GET(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.ORGANIZATION_BY_GROUPS)
    suspend fun listGroups(
        @Query("limit") limit: Int? = null,
        @Query("after") after: String? = null,
        @Query("order") order: String? = null
    ): site.addzero.api.openai.models.GroupListResource

    /**
     * Creates a new group in the organization. REST: POST /organization/groups
     */
    @POST(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.ORGANIZATION_BY_GROUPS)
    suspend fun createGroup(
        @Body body: site.addzero.api.openai.models.CreateGroupBody
    ): site.addzero.api.openai.models.GroupResponse

    /**
     * Updates a group's information. REST: POST /organization/groups/{group_id}
     */
    @POST(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.ORGANIZATION_BY_GROUPS_BY_GROUP_ID)
    suspend fun updateGroup(
        @Path("group_id") groupId: String,
        @Body body: site.addzero.api.openai.models.UpdateGroupBody
    ): site.addzero.api.openai.models.GroupResourceWithSuccess

    /**
     * Deletes a group from the organization. REST: DELETE /organization/groups/{group_id}
     */
    @DELETE(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.ORGANIZATION_BY_GROUPS_BY_GROUP_ID)
    suspend fun deleteGroup(
        @Path("group_id") groupId: String
    ): site.addzero.api.openai.models.GroupDeletedResource

}
