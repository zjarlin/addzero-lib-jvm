package site.addzero.configcenter.runtime

import kotlinx.serialization.json.Json
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single
import site.addzero.configcenter.spec.ConfigBridgeSpi
import site.addzero.configcenter.spec.ConfigCenterGateway
import site.addzero.configcenter.spec.ConfigEncryptionSpi
import site.addzero.configcenter.spec.ConfigRendererSpi
import site.addzero.configcenter.spec.ConfigRepositorySpi

@Module
class ConfigCenterRuntimeKoinModule {
    @Single
    fun provideJson(): Json {
        return Json {
            prettyPrint = true
            ignoreUnknownKeys = true
            coerceInputValues = true
        }
    }

    @Single
    fun provideBootstrap(): ConfigCenterBootstrap {
        return ConfigCenterBootstrap()
    }

    @Single
    fun provideDatabase(
        bootstrap: ConfigCenterBootstrap,
    ): ConfigCenterDatabase {
        return ConfigCenterDatabase(bootstrap)
    }

    @Single
    fun provideEncryption(
        bootstrap: ConfigCenterBootstrap,
    ): ConfigEncryptionSpi {
        return EnvMasterKeyEncryptionSpi(bootstrap)
    }

    @Single
    fun provideRepository(
        database: ConfigCenterDatabase,
        encryption: ConfigEncryptionSpi,
        json: Json,
    ): ConfigRepositorySpi {
        return JdbcConfigCenterRepository(
            database = database,
            encryption = encryption,
            json = json,
        )
    }

    @Single
    fun provideRenderer(
        json: Json,
    ): ConfigRendererSpi {
        return DefaultConfigRendererSpi(json)
    }

    @Single
    fun provideGateway(
        bootstrap: ConfigCenterBootstrap,
        repository: ConfigRepositorySpi,
        renderer: ConfigRendererSpi,
    ): ConfigCenterGateway {
        return JvmConfigCenterGateway(
            bootstrap = bootstrap,
            repository = repository,
            renderers = listOf(renderer),
        )
    }

    @Single
    fun provideCompatService(
        gateway: ConfigCenterGateway,
    ): ConfigCenterCompatService {
        return ConfigCenterCompatService(gateway)
    }

    @Single
    fun provideKtorBridge(
        gateway: ConfigCenterGateway,
        bootstrap: ConfigCenterBootstrap,
    ): KtorConfigBridge {
        return KtorConfigBridge(gateway, bootstrap)
    }

    @Single
    fun provideSpringBridge(
        gateway: ConfigCenterGateway,
        bootstrap: ConfigCenterBootstrap,
    ): SpringPropertySourceBridge {
        return SpringPropertySourceBridge(gateway, bootstrap)
    }

    @Single
    fun provideProcessEnvBridge(
        gateway: ConfigCenterGateway,
        bootstrap: ConfigCenterBootstrap,
    ): ProcessEnvBridge {
        return ProcessEnvBridge(gateway, bootstrap)
    }

    @Single
    fun provideGenericTemplateBridge(
        gateway: ConfigCenterGateway,
        bootstrap: ConfigCenterBootstrap,
    ): GenericTemplateBridge {
        return GenericTemplateBridge(gateway, bootstrap)
    }

    @Single
    fun provideBridgeList(
        ktorConfigBridge: KtorConfigBridge,
        springPropertySourceBridge: SpringPropertySourceBridge,
        processEnvBridge: ProcessEnvBridge,
        genericTemplateBridge: GenericTemplateBridge,
    ): List<ConfigBridgeSpi> {
        return listOf(
            ktorConfigBridge,
            springPropertySourceBridge,
            processEnvBridge,
            genericTemplateBridge,
        )
    }
}
