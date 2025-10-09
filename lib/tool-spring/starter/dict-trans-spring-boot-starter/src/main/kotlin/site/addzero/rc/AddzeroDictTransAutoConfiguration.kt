package site.addzero.rc

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(AddzeroDictTransProperties::class)
@ConditionalOnProperty(
    prefix = "site.addzero.scan.dict.trans",
    name = ["enabled"],
    havingValue = "true",
    matchIfMissing = true
)
class AddzeroDictTransAutoConfiguration {

}