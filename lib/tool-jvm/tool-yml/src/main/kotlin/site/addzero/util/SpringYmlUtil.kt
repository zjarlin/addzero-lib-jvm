package site.addzero.util

import site.addzero.util.YmlUtil.getActivateBydir
import site.addzero.util.YmlUtil.replaceEnvInString
import java.io.File

class SpringYmlUtil(val customPath: String?) {
    val actPath by lazy {
        if (customPath != null && File(customPath).exists()) {
            customPath
        } else {
            // 使用默认的resources目录路径
            val resource = Thread.currentThread().contextClassLoader.getResource("")
                ?: throw IllegalStateException("无法找到resources目录")
            File(resource.toURI()).absolutePath
        }
    }



    fun getYmlContent(resourceName: String): String {
        val file = getResource(resourceName)
        return file.readText()
    }

    private fun getResource(resourceName: String): File = File(actPath, resourceName)


    fun getActivateYml(): Map<String, Any> {
        val activate = getActivateBydir(getResource("application.yml").absolutePath)
        val ymlActivateAbsolutePath = getResource("application-$activate.yml").absolutePath
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
                      val replaceEnvInString = configValue.replaceEnvInString("")

       return replaceEnvInString
//        return configValue
    }


}
