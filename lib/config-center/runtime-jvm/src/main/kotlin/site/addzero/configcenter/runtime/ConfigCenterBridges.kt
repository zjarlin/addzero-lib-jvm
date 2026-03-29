package site.addzero.configcenter.runtime

import kotlinx.coroutines.runBlocking
import site.addzero.configcenter.spec.ConfigBridgeSpi
import site.addzero.configcenter.spec.ConfigCenterGateway
import site.addzero.configcenter.spec.ConfigQuery

class SpringPropertySourceBridge(
    private val gateway: ConfigCenterGateway,
    private val bootstrap: ConfigCenterBootstrap,
) : ConfigBridgeSpi {
    override val bridgeName: String = "spring-property-source"

    fun getProperties(
        namespace: String = bootstrap.appId,
        profile: String = bootstrap.profile,
    ): Map<String, Any> {
        return runBlocking {
            gateway.getSnapshot(
                namespace = namespace,
                profile = profile,
            )
        }.mapValues { (_, value) -> value }
    }

    fun getProperty(
        key: String,
        namespace: String = bootstrap.appId,
        profile: String = bootstrap.profile,
    ): String? {
        return runBlocking {
            gateway.getEnv(
                key = key,
                query = ConfigQuery(
                    namespace = namespace,
                    profile = profile,
                ),
            )
        }
    }
}

class ProcessEnvBridge(
    private val gateway: ConfigCenterGateway,
    private val bootstrap: ConfigCenterBootstrap,
) : ConfigBridgeSpi {
    override val bridgeName: String = "process-env"

    fun getEnvMap(
        namespace: String = bootstrap.appId,
        profile: String = bootstrap.profile,
    ): Map<String, String> {
        return runBlocking {
            gateway.getSnapshot(
                namespace = namespace,
                profile = profile,
            )
        }.mapKeys { (key, _) ->
            key.uppercase()
                .replace(Regex("[^A-Z0-9]+"), "_")
                .trim('_')
        }
    }
}

class GenericTemplateBridge(
    private val gateway: ConfigCenterGateway,
    private val bootstrap: ConfigCenterBootstrap,
) : ConfigBridgeSpi {
    override val bridgeName: String = "generic-template"

    fun render(
        templateText: String,
        namespace: String = bootstrap.appId,
        profile: String = bootstrap.profile,
    ): String {
        val snapshot = runBlocking {
            gateway.getSnapshot(
                namespace = namespace,
                profile = profile,
            )
        }
        return snapshot.entries.fold(templateText) { rendered, (key, value) ->
            rendered
                .replace("{{${key}}}", value)
                .replace("\${${key}}", value)
        }
    }
}
