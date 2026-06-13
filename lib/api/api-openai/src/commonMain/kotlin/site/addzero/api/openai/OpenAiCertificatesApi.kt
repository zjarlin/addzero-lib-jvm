// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai

import de.jensklingenberg.ktorfit.http.*
import site.addzero.api.openai.models.Certificate
import site.addzero.api.openai.models.DeleteCertificateResponse
import site.addzero.api.openai.models.ListCertificatesResponse
import site.addzero.api.openai.models.ListProjectCertificatesResponse
import site.addzero.api.openai.models.ModifyCertificateRequest
import site.addzero.api.openai.models.OrganizationCertificateActivationResponse
import site.addzero.api.openai.models.OrganizationCertificateDeactivationResponse
import site.addzero.api.openai.models.OrganizationProjectCertificateActivationResponse
import site.addzero.api.openai.models.OrganizationProjectCertificateDeactivationResponse
import site.addzero.api.openai.models.ToggleCertificatesRequest
import site.addzero.api.openai.models.UploadCertificateRequest

interface OpenAiCertificatesApi {

    /**
     * List uploaded certificates for this organization. REST: GET /organization/certificates
     */
    @GET(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.ORGANIZATION_BY_CERTIFICATES)
    suspend fun listOrganizationCertificates(
        @Query("limit") limit: Int? = null,
        @Query("after") after: String? = null,
        @Query("order") order: String? = null
    ): site.addzero.api.openai.models.ListCertificatesResponse

    /**
     * Upload a certificate to the organization. This does **not** automatically activate the certificate.
     * Organizations can upload up to 50 certificates. REST: POST /organization/certificates
     */
    @POST(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.ORGANIZATION_BY_CERTIFICATES)
    suspend fun uploadCertificate(
        @Body body: site.addzero.api.openai.models.UploadCertificateRequest
    ): site.addzero.api.openai.models.Certificate

    /**
     * Activate certificates at the organization level. You can atomically and idempotently activate up to
     * 10 certificates at a time. REST: POST /organization/certificates/activate
     */
    @POST(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.ORGANIZATION_BY_CERTIFICATES_BY_ACTIVATE)
    suspend fun activateOrganizationCertificates(
        @Body body: site.addzero.api.openai.models.ToggleCertificatesRequest
    ): site.addzero.api.openai.models.OrganizationCertificateActivationResponse

    /**
     * Deactivate certificates at the organization level. You can atomically and idempotently deactivate up
     * to 10 certificates at a time. REST: POST /organization/certificates/deactivate
     */
    @POST(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.ORGANIZATION_BY_CERTIFICATES_BY_DEACTIVATE)
    suspend fun deactivateOrganizationCertificates(
        @Body body: site.addzero.api.openai.models.ToggleCertificatesRequest
    ): site.addzero.api.openai.models.OrganizationCertificateDeactivationResponse

    /**
     * Get a certificate that has been uploaded to the organization. You can get a certificate regardless
     * of whether it is active or not. REST: GET /organization/certificates/{certificate_id}
     */
    @GET(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.ORGANIZATION_BY_CERTIFICATES_BY_CERTIFICATE_ID)
    suspend fun getCertificate(
        @Path("certificate_id") certificateId: String,
        @Query("include") include: List<String>? = null
    ): site.addzero.api.openai.models.Certificate

    /**
     * Modify a certificate. Note that only the name can be modified. REST: POST
     * /organization/certificates/{certificate_id}
     */
    @POST(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.ORGANIZATION_BY_CERTIFICATES_BY_CERTIFICATE_ID)
    suspend fun modifyCertificate(
        @Path("certificate_id") certificateId: String,
        @Body body: site.addzero.api.openai.models.ModifyCertificateRequest
    ): site.addzero.api.openai.models.Certificate

    /**
     * Delete a certificate from the organization. The certificate must be inactive for the organization
     * and all projects. REST: DELETE /organization/certificates/{certificate_id}
     */
    @DELETE(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.ORGANIZATION_BY_CERTIFICATES_BY_CERTIFICATE_ID)
    suspend fun deleteCertificate(
        @Path("certificate_id") certificateId: String
    ): site.addzero.api.openai.models.DeleteCertificateResponse

    /**
     * List certificates for this project. REST: GET /organization/projects/{project_id}/certificates
     */
    @GET(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.ORGANIZATION_BY_PROJECTS_BY_PROJECT_ID_BY_CERTIFICATES)
    suspend fun listProjectCertificates(
        @Path("project_id") projectId: String,
        @Query("limit") limit: Int? = null,
        @Query("after") after: String? = null,
        @Query("order") order: String? = null
    ): site.addzero.api.openai.models.ListProjectCertificatesResponse

    /**
     * Activate certificates at the project level. You can atomically and idempotently activate up to 10
     * certificates at a time. REST: POST /organization/projects/{project_id}/certificates/activate
     */
    @POST(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.ORGANIZATION_BY_PROJECTS_BY_PROJECT_ID_BY_CERTIFICATES_BY_ACTIVATE)
    suspend fun activateProjectCertificates(
        @Path("project_id") projectId: String,
        @Body body: site.addzero.api.openai.models.ToggleCertificatesRequest
    ): site.addzero.api.openai.models.OrganizationProjectCertificateActivationResponse

    /**
     * Deactivate certificates at the project level. You can atomically and idempotently deactivate up to
     * 10 certificates at a time. REST: POST /organization/projects/{project_id}/certificates/deactivate
     */
    @POST(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.ORGANIZATION_BY_PROJECTS_BY_PROJECT_ID_BY_CERTIFICATES_BY_DEACTIVATE)
    suspend fun deactivateProjectCertificates(
        @Path("project_id") projectId: String,
        @Body body: site.addzero.api.openai.models.ToggleCertificatesRequest
    ): site.addzero.api.openai.models.OrganizationProjectCertificateDeactivationResponse

}
