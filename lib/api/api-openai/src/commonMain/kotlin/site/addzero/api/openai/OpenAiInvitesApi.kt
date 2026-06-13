// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai

import de.jensklingenberg.ktorfit.http.*
import site.addzero.api.openai.models.Invite
import site.addzero.api.openai.models.InviteDeleteResponse
import site.addzero.api.openai.models.InviteListResponse
import site.addzero.api.openai.models.InviteRequest

interface OpenAiInvitesApi {

    /**
     * Returns a list of invites in the organization. REST: GET /organization/invites
     */
    @GET(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.ORGANIZATION_BY_INVITES)
    suspend fun listInvites(
        @Query("limit") limit: Int? = null,
        @Query("after") after: String? = null
    ): site.addzero.api.openai.models.InviteListResponse

    /**
     * Create an invite for a user to the organization. The invite must be accepted by the user before they
     * have access to the organization. REST: POST /organization/invites
     */
    @POST(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.ORGANIZATION_BY_INVITES)
    suspend fun inviteUser(
        @Body body: site.addzero.api.openai.models.InviteRequest
    ): site.addzero.api.openai.models.Invite

    /**
     * Retrieves an invite. REST: GET /organization/invites/{invite_id}
     */
    @GET(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.ORGANIZATION_BY_INVITES_BY_INVITE_ID)
    suspend fun retrieveInvite(
        @Path("invite_id") inviteId: String
    ): site.addzero.api.openai.models.Invite

    /**
     * Delete an invite. If the invite has already been accepted, it cannot be deleted. REST: DELETE
     * /organization/invites/{invite_id}
     */
    @DELETE(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.ORGANIZATION_BY_INVITES_BY_INVITE_ID)
    suspend fun deleteInvite(
        @Path("invite_id") inviteId: String
    ): site.addzero.api.openai.models.InviteDeleteResponse

}
