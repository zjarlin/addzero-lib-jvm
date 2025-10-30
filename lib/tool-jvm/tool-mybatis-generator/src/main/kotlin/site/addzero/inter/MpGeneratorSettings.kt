package site.addzero.inter

import site.addzero.util.SpringYmlUtil

interface MpGeneratorSettings {
    val resourcePath: String
    val authName: String
    val velocityContextMap: Map<String, Any>
    val urlYmlPath: String
    val usernameYmlPath: String
    val passwordYmlPath: String

    val springYmlUtil: SpringYmlUtil
        get() = SpringYmlUtil(resourcePath)


    val url: String
        get() {
            val activateYmlPropertiesString = springYmlUtil .getActivateYmlPropertiesString(urlYmlPath)
            return activateYmlPropertiesString
                ?: throw RuntimeException("请配置MpGeneratorSettings数据库连接信息${urlYmlPath}")
        }
    val username: String
        get() {
            val activateYmlPropertiesString = springYmlUtil .getActivateYmlPropertiesString(usernameYmlPath)
            return activateYmlPropertiesString
                ?: throw RuntimeException("请配置MpGeneratorSettings数据库连接信息${usernameYmlPath}")
        }
    val password: String
        get() {
            val activateYmlPropertiesString = springYmlUtil .getActivateYmlPropertiesString(passwordYmlPath)
            return activateYmlPropertiesString ?: throw RuntimeException("请配置MpGeneratorSettings数据库连接信息${passwordYmlPath}")
        }

}
