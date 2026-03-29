package site.addzero.configcenter.client

import io.ktor.client.HttpClient
import site.addzero.core.network.HttpClientFactory
import site.addzero.configcenter.spec.ConfigCenterGateway
import site.addzero.configcenter.spec.ConfigEntryDto
import site.addzero.configcenter.spec.ConfigMutationRequest
import site.addzero.configcenter.spec.ConfigQuery
import site.addzero.configcenter.spec.ConfigTargetDto
import site.addzero.configcenter.spec.ConfigTargetMutationRequest
import site.addzero.configcenter.spec.RenderedConfig

internal expect fun buildConfigCenterHttpApi(
    baseUrl: String,
    httpClient: HttpClient,
): ConfigCenterHttpApi

object ConfigCenterApiClient {
    private const val httpClientProfile = "config-center"
    private val httpClientFactory: HttpClientFactory
        get() = HttpClientFactory.shared()

    @Volatile
    private var baseUrl: String = "http://localhost:8080/"

    fun configureBaseUrl(
        value: String,
    ) {
        baseUrl = value.ifBlank { "http://localhost:8080/" }
    }

    fun createApi(): ConfigCenterHttpApi {
        return buildConfigCenterHttpApi(
            baseUrl = baseUrl,
            httpClient = httpClientFactory.get(httpClientProfile),
        )
    }
}

class ConfigCenterRemoteGateway(
    private val apiFactory: () -> ConfigCenterHttpApi = ConfigCenterApiClient::createApi,
) : ConfigCenterGateway {
    override suspend fun getEnv(
        key: String,
        query: ConfigQuery,
    ): String? {
        return apiFactory().getEnv(
            key = key,
            namespace = query.namespace,
            profile = query.profile,
            domain = query.domainQueryValue(),
        ).value
    }

    override suspend fun getSnapshot(
        namespace: String?,
        profile: String,
    ): Map<String, String> {
        return apiFactory().getSnapshot(
            namespace = namespace,
            profile = profile,
        ).items
    }

    override suspend fun listEntries(
        query: ConfigQuery,
    ): List<ConfigEntryDto> {
        return apiFactory().listEntries(
            namespace = query.namespace,
            domain = query.domainQueryValue(),
            profile = query.profile,
            keyword = query.keyword,
            includeDisabled = query.includeDisabled,
        )
    }

    override suspend fun getEntry(
        id: String,
    ): ConfigEntryDto? {
        return runCatching { apiFactory().getEntry(id) }.getOrNull()
    }

    override suspend fun addEnv(
        request: ConfigMutationRequest,
    ): ConfigEntryDto {
        return apiFactory().addEntry(request)
    }

    override suspend fun updateEnv(
        id: String,
        request: ConfigMutationRequest,
    ): ConfigEntryDto {
        return apiFactory().updateEntry(id, request)
    }

    override suspend fun deleteEnv(
        id: String,
    ) {
        apiFactory().deleteEntry(id)
    }

    override suspend fun listTargets(): List<ConfigTargetDto> {
        return apiFactory().listTargets()
    }

    override suspend fun getTarget(
        id: String,
    ): ConfigTargetDto? {
        return runCatching { apiFactory().getTarget(id) }.getOrNull()
    }

    override suspend fun saveTarget(
        request: ConfigTargetMutationRequest,
    ): ConfigTargetDto {
        val requestId = request.id
        return if (requestId.isNullOrBlank()) {
            apiFactory().saveTarget(request)
        } else {
            apiFactory().updateTarget(requestId, request)
        }
    }

    override suspend fun deleteTarget(
        id: String,
    ) {
        apiFactory().deleteTarget(id)
    }

    override suspend fun renderTarget(
        targetId: String,
    ): RenderedConfig {
        return apiFactory().previewTarget(targetId)
    }

    override suspend fun previewTarget(
        targetId: String,
    ): String {
        return apiFactory().previewTarget(targetId).content
    }

    override suspend fun exportTarget(
        targetId: String,
    ): RenderedConfig {
        return apiFactory().exportTarget(targetId)
    }
}

object ConfigCenter {
    @Volatile
    private var gateway: ConfigCenterGateway = ConfigCenterRemoteGateway()

    fun install(
        gateway: ConfigCenterGateway,
    ) {
        this.gateway = gateway
    }

    fun useRemoteGateway() {
        gateway = ConfigCenterRemoteGateway()
    }

    suspend fun getEnv(
        key: String,
        query: ConfigQuery = ConfigQuery(),
    ): String? {
        return gateway.getEnv(key, query)
    }

    suspend fun getSnapshot(
        namespace: String? = null,
        profile: String = "default",
    ): Map<String, String> {
        return gateway.getSnapshot(namespace, profile)
    }

    suspend fun listEntries(
        query: ConfigQuery = ConfigQuery(),
    ): List<ConfigEntryDto> {
        return gateway.listEntries(query)
    }

    suspend fun addEnv(
        request: ConfigMutationRequest,
    ): ConfigEntryDto {
        return gateway.addEnv(request)
    }

    suspend fun updateEnv(
        id: String,
        request: ConfigMutationRequest,
    ): ConfigEntryDto {
        return gateway.updateEnv(id, request)
    }

    suspend fun deleteEnv(
        id: String,
    ) {
        gateway.deleteEnv(id)
    }

    suspend fun listTargets(): List<ConfigTargetDto> {
        return gateway.listTargets()
    }

    suspend fun saveTarget(
        request: ConfigTargetMutationRequest,
    ): ConfigTargetDto {
        return gateway.saveTarget(request)
    }

    suspend fun deleteTarget(
        id: String,
    ) {
        gateway.deleteTarget(id)
    }

    suspend fun previewTarget(
        targetId: String,
    ): String {
        return gateway.previewTarget(targetId)
    }

    suspend fun renderTarget(
        targetId: String,
    ): RenderedConfig {
        return gateway.renderTarget(targetId)
    }

    suspend fun exportTarget(
        targetId: String,
    ): RenderedConfig {
        return gateway.exportTarget(targetId)
    }
}
