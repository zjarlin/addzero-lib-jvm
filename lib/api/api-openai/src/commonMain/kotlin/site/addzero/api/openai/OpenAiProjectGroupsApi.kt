// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai

import de.jensklingenberg.ktorfit.http.*
import site.addzero.api.openai.models.InviteProjectGroupBody
import site.addzero.api.openai.models.ProjectGroup
import site.addzero.api.openai.models.ProjectGroupDeletedResource
import site.addzero.api.openai.models.ProjectGroupListResource

interface OpenAiProjectGroupsApi {

    /**
     * Lists the groups that have access to a project. REST: GET /organization/projects/{project_id}/groups
     */
    @GET(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.ORGANIZATION_BY_PROJECTS_BY_PROJECT_ID_BY_GROUPS)
    suspend fun listProjectGroups(
        @Path("project_id") projectId: String,
        @Query("limit") limit: Int? = null,
        @Query("after") after: String? = null,
        @Query("order") order: String? = null
    ): site.addzero.api.openai.models.ProjectGroupListResource

    /**
     * Grants a group access to a project. REST: POST /organization/projects/{project_id}/groups
     */
    @POST(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.ORGANIZATION_BY_PROJECTS_BY_PROJECT_ID_BY_GROUPS)
    suspend fun addProjectGroup(
        @Path("project_id") projectId: String,
        @Body body: site.addzero.api.openai.models.InviteProjectGroupBody
    ): site.addzero.api.openai.models.ProjectGroup

    /**
     * Revokes a group's access to a project. REST: DELETE
     * /organization/projects/{project_id}/groups/{group_id}
     */
    @DELETE(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.ORGANIZATION_BY_PROJECTS_BY_PROJECT_ID_BY_GROUPS_BY_GROUP_ID)
    suspend fun removeProjectGroup(
        @Path("project_id") projectId: String,
        @Path("group_id") groupId: String
    ): site.addzero.api.openai.models.ProjectGroupDeletedResource

}
