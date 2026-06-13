// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.kcloud.api.openai

import de.jensklingenberg.ktorfit.http.*

/** Invites REST endpoints. */
interface OpenAiInvitesApi {

    /**
     * Returns a list of invites in the organization.
     *
     * REST: GET /organization/invites
     */
    @GET(OpenAiApiPaths.ORGANIZATION_BY_INVITES)
    suspend fun listInvites(
        @Query("limit") limit: Int? = null,
        @Query("after") after: String? = null
    ): OpenAiResponseBody

    /**
     * Create an invite for a user to the organization. The invite must be accepted by the user before they have access to the organization.
     *
     * REST: POST /organization/invites
     */
    @POST(OpenAiApiPaths.ORGANIZATION_BY_INVITES)
    suspend fun inviteUser(
        @Body body: OpenAiRequestBody
    ): OpenAiResponseBody

    /**
     * Delete an invite. If the invite has already been accepted, it cannot be deleted.
     *
     * REST: DELETE /organization/invites/{invite_id}
     */
    @DELETE(OpenAiApiPaths.ORGANIZATION_BY_INVITES_BY_INVITE_ID)
    suspend fun deleteInvite(
        @Path("invite_id") inviteId: String
    ): OpenAiResponseBody

    /**
     * Retrieves an invite.
     *
     * REST: GET /organization/invites/{invite_id}
     */
    @GET(OpenAiApiPaths.ORGANIZATION_BY_INVITES_BY_INVITE_ID)
    suspend fun retrieveInvite(
        @Path("invite_id") inviteId: String
    ): OpenAiResponseBody
}
