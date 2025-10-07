package site.addzero.web.infra.jimmer.dynamicdatasource

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "spring.datasource.dynamic")
class DynamicDatasourceProperties {

    var primary: String = DbEnum.POSTGRESQL.key
    var datasource: Map<String, DataSourceConfig> = HashMap()

    class DataSourceConfig {
        lateinit var driverClassName: String
        lateinit var url: String
        lateinit var username: String
        lateinit var password: String
        lateinit var excludeTables: String
    }
}
