package site.addzero.configcenter.client

import de.jensklingenberg.ktorfit.http.Body
import de.jensklingenberg.ktorfit.http.DELETE
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.Headers
import de.jensklingenberg.ktorfit.http.POST
import de.jensklingenberg.ktorfit.http.PUT
import de.jensklingenberg.ktorfit.http.Path
import de.jensklingenberg.ktorfit.http.Query
import site.addzero.configcenter.spec.ConfigEntryDto
import site.addzero.configcenter.spec.ConfigMutationRequest
import site.addzero.configcenter.spec.ConfigQuery
import site.addzero.configcenter.spec.ConfigSnapshotDto
import site.addzero.configcenter.spec.ConfigTargetDto
import site.addzero.configcenter.spec.ConfigTargetMutationRequest
import site.addzero.configcenter.spec.ConfigValueResponse
import site.addzero.configcenter.spec.RenderedConfig

interface ConfigCenterHttpApi {
    @GET("api/config-center/env")
    suspend fun getEnv(
        @Query("key") key: String,
        @Query("namespace") namespace: String? = null,
        @Query("profile") profile: String = "default",
        @Query("domain") domain: String? = null,
    ): ConfigValueResponse

    @GET("api/config-center/snapshot")
    suspend fun getSnapshot(
        @Query("namespace") namespace: String? = null,
        @Query("profile") profile: String = "default",
    ): ConfigSnapshotDto

    @GET("api/config-center/entries")
    suspend fun listEntries(
        @Query("namespace") namespace: String? = null,
        @Query("domain") domain: String? = null,
        @Query("profile") profile: String = "default",
        @Query("keyword") keyword: String? = null,
        @Query("includeDisabled") includeDisabled: Boolean = false,
    ): List<ConfigEntryDto>

    @GET("api/config-center/entries/{id}")
    suspend fun getEntry(
        @Path("id") id: String,
    ): ConfigEntryDto

    @Headers("Content-Type: application/json")
    @POST("api/config-center/entries")
    suspend fun addEntry(
        @Body request: ConfigMutationRequest,
    ): ConfigEntryDto

    @Headers("Content-Type: application/json")
    @PUT("api/config-center/entries/{id}")
    suspend fun updateEntry(
        @Path("id") id: String,
        @Body request: ConfigMutationRequest,
    ): ConfigEntryDto

    @DELETE("api/config-center/entries/{id}")
    suspend fun deleteEntry(
        @Path("id") id: String,
    )

    @GET("api/config-center/targets")
    suspend fun listTargets(): List<ConfigTargetDto>

    @GET("api/config-center/targets/{id}")
    suspend fun getTarget(
        @Path("id") id: String,
    ): ConfigTargetDto

    @Headers("Content-Type: application/json")
    @POST("api/config-center/targets")
    suspend fun saveTarget(
        @Body request: ConfigTargetMutationRequest,
    ): ConfigTargetDto

    @Headers("Content-Type: application/json")
    @PUT("api/config-center/targets/{id}")
    suspend fun updateTarget(
        @Path("id") id: String,
        @Body request: ConfigTargetMutationRequest,
    ): ConfigTargetDto

    @DELETE("api/config-center/targets/{id}")
    suspend fun deleteTarget(
        @Path("id") id: String,
    )

    @POST("api/config-center/render/{targetId}/preview")
    suspend fun previewTarget(
        @Path("targetId") targetId: String,
    ): RenderedConfig

    @POST("api/config-center/render/{targetId}/export")
    suspend fun exportTarget(
        @Path("targetId") targetId: String,
    ): RenderedConfig

    @GET("api/config-center/bootstrap/{key}")
    suspend fun getBootstrapValue(
        @Path("key") key: String,
    ): ConfigValueResponse
}

internal fun ConfigQuery.domainQueryValue(): String? {
    return domain?.name
}
