package site.addzero.web.infra.advice

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.EnableAspectJAutoProxy

@Configuration
@EnableAspectJAutoProxy
@ComponentScan(basePackages = ["site.addzero.web.infra.advice"])
@ConditionalOnProperty(
    prefix = "site.addzero.scan.controller.advice",
    name = ["enabled"],
    havingValue = "true",
    matchIfMissing = true
)
class AutoRc