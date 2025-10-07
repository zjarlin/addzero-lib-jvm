package site.addzero.rc

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(ScanControllerProperties::class)
@ConditionalOnProperty(
    prefix = "expression.scan.controller",
    name = ["enabled"],
    havingValue = "true",
    matchIfMissing = true
)
class ExpressionScanAutoConfiguration {

}
