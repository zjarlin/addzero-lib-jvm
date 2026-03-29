package site.addzero.kcloud.jimmer.spi

import site.addzero.kcloud.jimmer.di.DatasourceProperties
import javax.sql.DataSource

data class DatasourceBootstrapContext(
    val properties: DatasourceProperties,
    val driver: DatabaseDriverSpi,
    val dataSource: DataSource,
)

interface JimmerDatasourceBootstrapSpi {
    val order: Int
        get() = 0

    fun supports(
        context: DatasourceBootstrapContext,
    ): Boolean = true

    fun onDataSourceReady(
        context: DatasourceBootstrapContext,
    )
}
