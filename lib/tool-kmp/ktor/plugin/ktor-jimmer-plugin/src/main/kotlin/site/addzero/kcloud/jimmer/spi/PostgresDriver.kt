package site.addzero.kcloud.jimmer.spi

import org.babyfish.jimmer.sql.dialect.Dialect
import org.babyfish.jimmer.sql.dialect.PostgresDialect
import org.koin.core.annotation.Single
import org.postgresql.ds.PGSimpleDataSource
import site.addzero.kcloud.jimmer.di.DatasourceProperties
import javax.sql.DataSource

@Single
class PostgresDriver : DatabaseDriverSpi {

    override fun supports(driver: String): Boolean =
        driver.contains("postgres", ignoreCase = true)

    override fun createDataSource(props: DatasourceProperties): DataSource =
        PGSimpleDataSource().apply {
            setURL(props.url)
            val queryParams = props.url.substringAfter('?', "")
                .split('&')
                .mapNotNull { entry ->
                    val separatorIndex = entry.indexOf('=')
                    if (separatorIndex <= 0) {
                        null
                    } else {
                        entry.substring(0, separatorIndex) to entry.substring(separatorIndex + 1)
                    }
                }
                .toMap()

            user = props.user.ifBlank { queryParams["user"] }.takeUnless { it.isNullOrBlank() }
            password = props.password.ifBlank { queryParams["password"] }.takeUnless { it.isNullOrBlank() }
        }

    override fun dialect(): Dialect = PostgresDialect()
}
