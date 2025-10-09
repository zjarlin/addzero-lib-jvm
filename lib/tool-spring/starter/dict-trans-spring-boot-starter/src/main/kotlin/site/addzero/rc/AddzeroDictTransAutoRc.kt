package site.addzero.rc

import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.EnableAspectJAutoProxy
import org.springframework.context.annotation.Primary

@Configuration
@EnableAspectJAutoProxy
@ComponentScan(basePackages = ["site.addzero"])
@Primary
class AddzeroDictTransAutoRc
