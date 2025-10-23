package site.addzero.util

import site.addzero.util.YmlUtil.getActivate

object SpringYmlUtil {

    fun getYmlAbsolutePath(resourceName: String = "application.yml"): String {
        val resource = Thread.currentThread().contextClassLoader.getResource(resourceName)
            ?: throw IllegalArgumentException("无法在classpath中找到资源文件: $resourceName")
        return resource.path
    }

    fun getActivateYml(): Map<String, Any> {
        val ymlAbsolutePath = getYmlAbsolutePath()
        val activate = getActivate(ymlAbsolutePath)
        val ymlActivateAbsolutePath = getYmlAbsolutePath("application-$activate.yml")
        val loadYmlConfigMap = YmlUtil.loadYmlConfigMap(ymlActivateAbsolutePath)
        return loadYmlConfigMap
    }

    fun <T> getActivateYmlProperties(pas: String): T? {
        val activateYml = getActivateYml()
        val configValue = YmlUtil.getConfigValue<T>(activateYml, pas)
        return configValue
    }
    fun getActivateYmlPropertiesString(pas: String): String? {
        val activateYml = getActivateYml()
        val configValue = YmlUtil.getConfigValue<String>(activateYml, pas)
        return configValue
    }


}
