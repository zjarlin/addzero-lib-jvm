package site.addzero.inter

import site.addzero.util.SpringYmlUtil

interface MpGeneratorSettings {
    val pkg: String
        get() = "site.addzero"
    val authName: String
        get() = "zjarlin"
    val extBaseApiImport: String
        get() = "$pkg.common.core.domain.BaseApi"
    val extExcelApiImport: String
        get() = "$pkg.common.core.domain.BaseExcelApi"


    val urlYmlPath: String
        get() = "spring.datasource.druid.master.url"
    val usernameYmlPath: String
        get() = "spring.datasource.druid.master.username"
    val passwordYmlPath: String
        get() = "spring.datasource.druid.master.password"

    val url: String
        get() {
            val activateYmlPropertiesString = SpringYmlUtil.getActivateYmlPropertiesString(urlYmlPath)
            return activateYmlPropertiesString ?: throw RuntimeException("请配置MpGeneratorSettings数据库连接信息${urlYmlPath}")
        }
    val username: String
        get() {
            val activateYmlPropertiesString = SpringYmlUtil.getActivateYmlPropertiesString(usernameYmlPath)
            return activateYmlPropertiesString ?: throw RuntimeException("请配置MpGeneratorSettings数据库连接信息${usernameYmlPath}")
        }
    val password: String
        get() {
            val activateYmlPropertiesString = SpringYmlUtil.getActivateYmlPropertiesString(passwordYmlPath)
            return activateYmlPropertiesString ?: throw RuntimeException("请配置MpGeneratorSettings数据库连接信息${passwordYmlPath}")
        }

}
