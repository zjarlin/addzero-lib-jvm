// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.kcloud.api.openai

import de.jensklingenberg.ktorfit.http.*

/** Certificates REST endpoints. */
interface OpenAiCertificatesApi {

    /**
     * List uploaded certificates for this organization.
     *
     * REST: GET /organization/certificates
     */
    @GET(OpenAiApiPaths.ORGANIZATION_BY_CERTIFICATES)
    suspend fun listOrganizationCertificates(
        @Query("limit") limit: Int? = null,
        @Query("after") after: String? = null,
        @Query("order") order: String? = null
    ): OpenAiResponseBody

    /**
     * Upload a certificate to the organization. This does **not** automatically activate the certificate. Organizations can upload up to 50 certificates.
     *
     * REST: POST /organization/certificates
     */
    @POST(OpenAiApiPaths.ORGANIZATION_BY_CERTIFICATES)
    suspend fun uploadCertificate(
        @Body body: OpenAiRequestBody
    ): OpenAiResponseBody

    /**
     * Activate certificates at the organization level. You can atomically and idempotently activate up to 10 certificates at a time.
     *
     * REST: POST /organization/certificates/activate
     */
    @POST(OpenAiApiPaths.ORGANIZATION_BY_CERTIFICATES_BY_ACTIVATE)
    suspend fun activateOrganizationCertificates(
        @Body body: OpenAiRequestBody
    ): OpenAiResponseBody

    /**
     * Deactivate certificates at the organization level. You can atomically and idempotently deactivate up to 10 certificates at a time.
     *
     * REST: POST /organization/certificates/deactivate
     */
    @POST(OpenAiApiPaths.ORGANIZATION_BY_CERTIFICATES_BY_DEACTIVATE)
    suspend fun deactivateOrganizationCertificates(
        @Body body: OpenAiRequestBody
    ): OpenAiResponseBody

    /**
     * Delete a certificate from the organization. The certificate must be inactive for the organization and all projects.
     *
     * REST: DELETE /organization/certificates/{certificate_id}
     */
    @DELETE(OpenAiApiPaths.ORGANIZATION_BY_CERTIFICATES_BY_CERTIFICATE_ID)
    suspend fun deleteCertificate(
        @Path("certificate_id") certificateId: String
    ): OpenAiResponseBody

    /**
     * Get a certificate that has been uploaded to the organization. You can get a certificate regardless of whether it is active or not.
     *
     * REST: GET /organization/certificates/{certificate_id}
     */
    @GET(OpenAiApiPaths.ORGANIZATION_BY_CERTIFICATES_BY_CERTIFICATE_ID)
    suspend fun getCertificate(
        @Path("certificate_id") certificateId: String,
        @Query("include") include: List<String>? = null
    ): OpenAiResponseBody

    /**
     * Modify a certificate. Note that only the name can be modified.
     *
     * REST: POST /organization/certificates/{certificate_id}
     */
    @POST(OpenAiApiPaths.ORGANIZATION_BY_CERTIFICATES_BY_CERTIFICATE_ID)
    suspend fun modifyCertificate(
        @Path("certificate_id") certificateId: String,
        @Body body: OpenAiRequestBody
    ): OpenAiResponseBody

    /**
     * List certificates for this project.
     *
     * REST: GET /organization/projects/{project_id}/certificates
     */
    @GET(OpenAiApiPaths.ORGANIZATION_BY_PROJECTS_BY_PROJECT_ID_BY_CERTIFICATES)
    suspend fun listProjectCertificates(
        @Path("project_id") projectId: String,
        @Query("limit") limit: Int? = null,
        @Query("after") after: String? = null,
        @Query("order") order: String? = null
    ): OpenAiResponseBody

    /**
     * Activate certificates at the project level. You can atomically and idempotently activate up to 10 certificates at a time.
     *
     * REST: POST /organization/projects/{project_id}/certificates/activate
     */
    @POST(OpenAiApiPaths.ORGANIZATION_BY_PROJECTS_BY_PROJECT_ID_BY_CERTIFICATES_BY_ACTIVATE)
    suspend fun activateProjectCertificates(
        @Path("project_id") projectId: String,
        @Body body: OpenAiRequestBody
    ): OpenAiResponseBody

    /**
     * Deactivate certificates at the project level. You can atomically and idempotently deactivate up to 10 certificates at a time.
     *
     * REST: POST /organization/projects/{project_id}/certificates/deactivate
     */
    @POST(OpenAiApiPaths.ORGANIZATION_BY_PROJECTS_BY_PROJECT_ID_BY_CERTIFICATES_BY_DEACTIVATE)
    suspend fun deactivateProjectCertificates(
        @Path("project_id") projectId: String,
        @Body body: OpenAiRequestBody
    ): OpenAiResponseBody
}
