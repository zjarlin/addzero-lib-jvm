package site.addzero.configcenter.runtime

import site.addzero.configcenter.spec.ConfigCenterGateway
import site.addzero.configcenter.spec.ConfigDomain
import site.addzero.configcenter.spec.ConfigMutationRequest
import site.addzero.configcenter.spec.ConfigQuery
import site.addzero.configcenter.spec.ConfigStorageMode
import site.addzero.configcenter.spec.ConfigValueType

class ConfigCenterCompatService(
    private val gateway: ConfigCenterGateway,
) {
    suspend fun getOrImportLegacyValue(
        namespace: String,
        key: String,
        description: String? = null,
        legacyLoader: (() -> String?)? = null,
    ): String? {
        val existing = gateway.getEnv(
            key = key,
            query = ConfigQuery(namespace = namespace),
        )
        if (existing != null) {
            return existing
        }
        val legacyValue = legacyLoader?.invoke() ?: return null
        saveLegacyValue(
            namespace = namespace,
            key = key,
            value = legacyValue,
            description = description,
        )
        return legacyValue
    }

    suspend fun saveLegacyValue(
        namespace: String,
        key: String,
        value: String,
        description: String? = null,
    ) {
        val existing = gateway.listEntries(
            ConfigQuery(
                namespace = namespace,
                includeDisabled = true,
            ),
        ).firstOrNull { entry -> entry.key == key }

        val request = ConfigMutationRequest(
            id = existing?.id,
            key = key,
            namespace = namespace,
            domain = ConfigDomain.BUSINESS,
            profile = existing?.profile ?: "default",
            valueType = ConfigValueType.STRING,
            storageMode = key.toLegacyStorageMode(),
            value = value,
            description = description ?: existing?.description,
            tags = existing?.tags ?: emptyList(),
            enabled = existing?.enabled ?: true,
        )

        if (existing == null) {
            gateway.addEnv(request)
        } else {
            gateway.updateEnv(existing.id, request)
        }
    }

    private fun String.toLegacyStorageMode(): ConfigStorageMode {
        val lowered = lowercase()
        return if (
            lowered.contains("token") ||
            lowered.contains("secret") ||
            lowered.contains("password") ||
            lowered.contains("accesskey")
        ) {
            ConfigStorageMode.REPO_ENCRYPTED
        } else {
            ConfigStorageMode.REPO_PLAIN
        }
    }
}

