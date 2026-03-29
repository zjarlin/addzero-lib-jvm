package site.addzero.kcloud.jimmer.di

import io.ktor.server.config.ApplicationConfig
import org.babyfish.jimmer.sql.kt.KSqlClient
import org.babyfish.jimmer.sql.kt.newKSqlClient
import org.babyfish.jimmer.sql.dialect.SQLiteDialect
import org.babyfish.jimmer.sql.runtime.DefaultDatabaseNamingStrategy
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.annotation.Property
import org.koin.core.annotation.Single
import site.addzero.kcloud.jimmer.interceptor.BaseEntityDraftInterceptor
import site.addzero.kcloud.jimmer.spi.DatabaseDriverSpi
import site.addzero.kcloud.jimmer.spi.DatasourceBootstrapContext
import site.addzero.kcloud.jimmer.spi.JimmerDatasourceBootstrapSpi
import java.util.concurrent.ConcurrentHashMap
import javax.sql.DataSource

private const val DEFAULT_EMBEDDED_SQLITE_URL = "jdbc:sqlite:jimmer-embedded.db"

/**
 * 数据源属性（从 application.conf 的 datasources.xxx 读取）
 */
data class DatasourceProperties(
    val name: String = "",
    val enabled: Boolean = false,
    val url: String = "",
    val driver: String = "",
    val user: String = "",
    val password: String = "",
)

@Module
@ComponentScan("site.addzero.kcloud.jimmer")
class JimmerKoinModule {
    @Single
    fun provideDefaultDataSource(registry: DatasourceRegistry): DataSource {
        return registry.defaultDataSource()
    }

    @Single
    fun provideDefaultSqlClient(registry: DatasourceRegistry): KSqlClient {
        return registry.defaultSqlClient()
    }
}

@Single
class DatabaseDriverResolver(
    private val drivers: List<DatabaseDriverSpi>,
) {
    fun resolve(driver: String): DatabaseDriverSpi {
        return drivers.firstOrNull { it.supports(driver) }
            ?: throw IllegalArgumentException(
                "No DatabaseDriverSpi found for driver '$driver'. Available: ${drivers.map { it::class.simpleName }}",
            )
    }
}

/**
 * 公共 KSqlClient 构建器
 */
private fun buildSqlClient(
    dataSource: DataSource,
    spi: DatabaseDriverSpi,
    interceptor: BaseEntityDraftInterceptor,
): KSqlClient = newKSqlClient {
    val dialect = spi.dialect()
    setDialect(dialect)
    addDraftInterceptor(interceptor)
    setDatabaseNamingStrategy(DefaultDatabaseNamingStrategy.LOWER_CASE)
    if (dialect is SQLiteDialect) {
        addScalarProvider(SqliteLocalDateTimeScalarProvider)
        addScalarProvider(SqliteInstantScalarProvider)
    }
    setConnectionManager { dataSource.connection.use { proceed(it) } }
}

private data class DatasourceRuntime(
    val properties: DatasourceProperties,
    val dataSource: DataSource,
    val sqlClient: KSqlClient,
)

@Single(createdAtStart = true)
class DatasourceRegistry(
    @Property(JIMMER_APPLICATION_CONFIG_PROPERTY)
    private val config: ApplicationConfig,
    private val interceptor: BaseEntityDraftInterceptor,
    private val driverResolver: DatabaseDriverResolver,
    private val datasourceBootstrappers: List<JimmerDatasourceBootstrapSpi>,
) {
    private val runtimesByName: LinkedHashMap<String, DatasourceRuntime> = buildRuntimeMap(config)
    private val defaultRuntime: DatasourceRuntime = runtimesByName.values.firstOrNull()
        ?: throw IllegalStateException(
            "No enabled datasource configured. Please configure at least one datasources.*.enabled=true entry.",
        )

    fun defaultDataSource(): DataSource {
        return defaultRuntime.dataSource
    }

    fun defaultSqlClient(): KSqlClient {
        return defaultRuntime.sqlClient
    }

    fun dataSource(name: String): DataSource {
        return runtime(name).dataSource
    }

    fun sqlClient(name: String): KSqlClient {
        return runtime(name).sqlClient
    }

    private fun runtime(name: String): DatasourceRuntime {
        return runtimesByName[name]
            ?: throw IllegalArgumentException(
                "Datasource '$name' is not configured. Available: ${runtimesByName.keys.joinToString()}",
            )
    }

    private fun buildRuntimeMap(config: ApplicationConfig): LinkedHashMap<String, DatasourceRuntime> {
        val datasources = loadAllDatasources(config)
            .filter { it.enabled }
            .ifEmpty {
                fallbackEmbeddedDesktopDatasource(config)?.let(::listOf) ?: emptyList()
            }

        val runtimes = LinkedHashMap<String, DatasourceRuntime>()
        for (props in datasources) {
            val spi = driverResolver.resolve(props.driver)
            val dataSource = spi.createDataSource(props)
            val context = DatasourceBootstrapContext(
                properties = props,
                driver = spi,
                dataSource = dataSource,
            )
            datasourceBootstrappers
                .sortedBy(JimmerDatasourceBootstrapSpi::order)
                .filter { bootstrapper -> bootstrapper.supports(context) }
                .forEach { bootstrapper -> bootstrapper.onDataSourceReady(context) }
            val sqlClient = buildSqlClient(dataSource, spi, interceptor)
            runtimes[props.name] = DatasourceRuntime(props, dataSource, sqlClient)
        }
        return runtimes
    }
}

private fun fallbackEmbeddedDesktopDatasource(config: ApplicationConfig): DatasourceProperties? {
    val isEmbeddedDesktop = System.getProperty(JIMMER_EMBEDDED_DESKTOP_MODE_PROPERTY)
        ?.toBooleanStrictOrNull()
        ?: false
    if (!isEmbeddedDesktop) {
        return null
    }

    val sqliteUrl = config.propertyOrNull("datasources.sqlite.url")
        ?.getString()
        ?.takeIf { it.isNotBlank() }
        ?: DEFAULT_EMBEDDED_SQLITE_URL
    val sqliteDriver = config.propertyOrNull("datasources.sqlite.driver")
        ?.getString()
        ?.takeIf { it.isNotBlank() }
        ?: "org.sqlite.SQLiteDriver"

    return DatasourceProperties(
        name = "sqlite",
        enabled = true,
        url = sqliteUrl,
        driver = sqliteDriver,
    )
}

/**
 * 动态数据源管理器（运行时按 dbType + url 创建临时连接，同样走 SPI）
 */
@Single
class DataSourceManager(
    private val interceptor: BaseEntityDraftInterceptor,
    private val driverResolver: DatabaseDriverResolver,
) {
    private val cache = ConcurrentHashMap<String, KSqlClient>()

    fun getSqlClient(dbType: String, url: String): KSqlClient {
        val key = "$dbType:$url"
        return cache.getOrPut(key) {
            val spi = driverResolver.resolve(dbType)
            val props = DatasourceProperties(name = key, enabled = true, url = url, driver = dbType)
            val dataSource = spi.createDataSource(props)
            buildSqlClient(dataSource, spi, interceptor)
        }
    }
}

private fun loadAllDatasources(config: ApplicationConfig): List<DatasourceProperties> {
    val datasources = mutableListOf<DatasourceProperties>()
    val datasourceConfig = runCatching { config.config("datasources") }.getOrNull() ?: return emptyList()

    for (name in datasourceConfig.toMap().keys) {
        val section = runCatching { datasourceConfig.config(name) }.getOrNull() ?: continue
        datasources += DatasourceProperties(
            name = name,
            enabled = section.propertyOrNull("enabled")?.getString()?.toBoolean() == true,
            url = section.propertyOrNull("url")?.getString().orEmpty(),
            driver = section.propertyOrNull("driver")?.getString().orEmpty(),
            user = section.propertyOrNull("user")?.getString().orEmpty(),
            password = section.propertyOrNull("password")?.getString().orEmpty(),
        )
    }

    return datasources
}
