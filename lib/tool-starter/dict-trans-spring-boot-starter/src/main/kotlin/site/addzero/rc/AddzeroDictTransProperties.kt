package site.addzero.rc

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "site.addzero.scan.dict.trans")
class AddzeroDictTransProperties(
    /**
     * 如果不配置则复用 ScanControllerProperties.pkg
     */
    var pkg: String? = null,
    /**
     * 如果不配置则复用 ScanControllerProperties.expression
     */
    var expression: String? = null,
)
