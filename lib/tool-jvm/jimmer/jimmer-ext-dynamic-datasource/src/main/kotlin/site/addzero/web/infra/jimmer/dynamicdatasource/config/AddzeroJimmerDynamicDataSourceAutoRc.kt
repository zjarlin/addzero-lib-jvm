package site.addzero.web.infra.jimmer.dynamicdatasource.config

import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.EnableAspectJAutoProxy

@Configuration
@EnableAspectJAutoProxy
@ComponentScan(basePackages = ["site.addzero.web.infra.jimmer.dynamicdatasource"])
class AddzeroJimmerDynamicDataSourceAutoRc
