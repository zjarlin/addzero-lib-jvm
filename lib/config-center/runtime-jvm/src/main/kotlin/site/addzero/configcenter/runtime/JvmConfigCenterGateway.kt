package site.addzero.configcenter.runtime

import kotlinx.serialization.json.Json
import site.addzero.configcenter.client.ConfigCenter
import site.addzero.configcenter.spec.ConfigCenterGateway
import site.addzero.configcenter.spec.ConfigEntryDto
import site.addzero.configcenter.spec.ConfigMutationRequest
import site.addzero.configcenter.spec.ConfigQuery
import site.addzero.configcenter.spec.ConfigRendererSpi
import site.addzero.configcenter.spec.ConfigRepositorySpi
import site.addzero.configcenter.spec.ConfigStorageMode
import site.addzero.configcenter.spec.ConfigTargetDto
import site.addzero.configcenter.spec.ConfigTargetMutationRequest
import site.addzero.configcenter.spec.RenderedConfig

class JvmConfigCenterGateway(
    private val bootstrap: ConfigCenterBootstrap,
    private val repository: ConfigRepositorySpi,
    private val renderers: List<ConfigRendererSpi>,
) : ConfigCenterGateway {
    override suspend fun getEnv(
        key: String,
        query: ConfigQuery,
    ): String? {
        return repository.findEntriesByKey(
            key = key,
            query = query.withDefaults(bootstrap),
        ).resolveWinningEntry()?.value
    }

    override suspend fun getSnapshot(
        namespace: String?,
        profile: String,
    ): Map<String, String> {
        return repository.listEntries(
            ConfigQuery(
                namespace = namespace ?: bootstrap.appId,
                profile = profile,
                includeDisabled = false,
            ),
        ).resolveLogicalEntries().mapNotNull { entry ->
            entry.value?.let { value -> entry.key to value }
        }.toMap()
    }

    override suspend fun listEntries(
        query: ConfigQuery,
    ): List<ConfigEntryDto> {
        return repository.listEntries(query)
    }

    override suspend fun getEntry(
        id: String,
    ): ConfigEntryDto? {
        return repository.getEntry(id)
    }

    override suspend fun addEnv(
        request: ConfigMutationRequest,
    ): ConfigEntryDto {
        return repository.upsertEntry(request.withDefaults(bootstrap))
    }

    override suspend fun updateEnv(
        id: String,
        request: ConfigMutationRequest,
    ): ConfigEntryDto {
        return repository.upsertEntry(request.copy(id = id).withDefaults(bootstrap))
    }

    override suspend fun deleteEnv(
        id: String,
    ) {
        repository.deleteEntry(id)
    }

    override suspend fun listTargets(): List<ConfigTargetDto> {
        return repository.listTargets()
    }

    override suspend fun getTarget(
        id: String,
    ): ConfigTargetDto? {
        return repository.getTarget(id)
    }

    override suspend fun saveTarget(
        request: ConfigTargetMutationRequest,
    ): ConfigTargetDto {
        return repository.upsertTarget(
            request.copy(
                profile = request.profile.ifBlank { bootstrap.profile },
            ),
        )
    }

    override suspend fun deleteTarget(
        id: String,
    ) {
        repository.deleteTarget(id)
    }

    override suspend fun renderTarget(
        targetId: String,
    ): RenderedConfig {
        val target = requireNotNull(repository.getTarget(targetId)) {
            "未找到渲染目标：$targetId"
        }
        val renderer = renderers.firstOrNull { it.supports(target.targetKind) }
            ?: error("未找到 ${target.targetKind} 的渲染器")
        val resolvedEntries = repository.listEntries(
            ConfigQuery(
                profile = target.profile.ifBlank { bootstrap.profile },
                includeDisabled = false,
            ),
        ).filterByNamespaces(target.namespaceFilter)
            .resolveLogicalEntries()
        return RenderedConfig(
            targetId = target.id,
            targetName = target.name,
            targetKind = target.targetKind,
            outputPath = target.outputPath,
            content = renderer.render(target, resolvedEntries),
        )
    }

    override suspend fun previewTarget(
        targetId: String,
    ): String {
        return renderTarget(targetId).content
    }

    override suspend fun exportTarget(
        targetId: String,
    ): RenderedConfig {
        val rendered = renderTarget(targetId).copy(
            exportedAtEpochMillis = System.currentTimeMillis(),
        )
        writeRenderedConfig(rendered)
        repository.writeBundleMeta("bundle.lastExportAt", rendered.exportedAtEpochMillis.toString())
        return rendered
    }

    companion object {
        fun createDefault(
            options: ConfigCenterBootstrapOptions = ConfigCenterBootstrapOptions(),
        ): JvmConfigCenterGateway {
            val json = Json {
                prettyPrint = true
                ignoreUnknownKeys = true
                coerceInputValues = true
            }
            val bootstrap = ConfigCenterBootstrap(options)
            val database = ConfigCenterDatabase(bootstrap)
            val encryption = EnvMasterKeyEncryptionSpi(bootstrap)
            val repository = JdbcConfigCenterRepository(
                database = database,
                encryption = encryption,
                json = json,
            )
            return JvmConfigCenterGateway(
                bootstrap = bootstrap,
                repository = repository,
                renderers = listOf(DefaultConfigRendererSpi(json)),
            )
        }
    }
}

fun installJvmConfigCenter(
    options: ConfigCenterBootstrapOptions = ConfigCenterBootstrapOptions(),
): ConfigCenterGateway {
    val gateway = JvmConfigCenterGateway.createDefault(options)
    ConfigCenter.install(gateway)
    return gateway
}

private fun ConfigQuery.withDefaults(
    bootstrap: ConfigCenterBootstrap,
): ConfigQuery {
    return copy(
        namespace = namespace ?: bootstrap.appId,
        profile = profile.ifBlank { bootstrap.profile },
    )
}

private fun ConfigMutationRequest.withDefaults(
    bootstrap: ConfigCenterBootstrap,
): ConfigMutationRequest {
    return copy(
        namespace = namespace.ifBlank { bootstrap.appId },
        profile = profile.ifBlank { bootstrap.profile },
    )
}

private fun List<ConfigEntryDto>.resolveWinningEntry(): ConfigEntryDto? {
    return sortedWith(
        compareBy<ConfigEntryDto> { entry ->
            when (entry.storageMode) {
                ConfigStorageMode.LOCAL_OVERRIDE -> 0
                else -> 1
            }
        }.thenByDescending { entry -> entry.updatedAtEpochMillis },
    ).firstOrNull()
}

private fun List<ConfigEntryDto>.resolveLogicalEntries(): List<ConfigEntryDto> {
    return groupBy { entry -> "${entry.namespace}:${entry.key}" }
        .values
        .mapNotNull { group -> group.resolveWinningEntry() }
        .sortedWith(compareBy<ConfigEntryDto> { it.namespace }.thenBy { it.key })
}

private fun List<ConfigEntryDto>.filterByNamespaces(
    namespaceFilter: String?,
): List<ConfigEntryDto> {
    val namespaces = namespaceFilter
        ?.split(",")
        ?.map { it.trim() }
        ?.filter { it.isNotEmpty() }
        ?.toSet()
        ?: return this
    return filter { entry -> entry.namespace in namespaces }
}

