package site.addzero.util

import cn.hutool.core.io.FileUtil
import site.addzero.util.YmlUtil.getActivateBydir
import site.addzero.util.YmlUtil.replaceEnvInString
import java.io.File

class SpringYmlUtil(val customPath: String?) {
    val actPath by lazy {
        if (customPath != null && FileUtil.file(customPath).exists()) {
            customPath
        } else {
            // 使用默认的resources目录路径
            val resource = Thread.currentThread().contextClassLoader.getResource("")
                ?: throw IllegalStateException("无法找到resources目录")
            FileUtil.file(resource.toURI()).absolutePath
        }
    }


    fun getYmlContent(resourceName: String): String {
        val file = getYmlResource(resourceName)
        return file.readText()
    }

    private fun getYmlResource(resourceName: String): File {
        // 获取基准名称（去除扩展名）
        val baseName = resourceName.removeSuffix(".yml").removeSuffix(".yaml")

        // 按顺序尝试查找文件
        val extensions = if (resourceName.contains(".")) listOf("", ".yml", ".yaml") else listOf(".yml", ".yaml")

        for (ext in extensions) {
            val file = FileUtil.file(actPath, "$baseName$ext")
            if (file.exists()) {
                return file
            }
        }

        // 如果都没找到，返回第一个尝试的文件（保持原有行为）
        return FileUtil.file(actPath, "$baseName${extensions.first()}")
    }


    fun getActivateYml(): Map<String, Any> {
        // 先查找主配置文件，支持.yml和.yaml扩展名
        val mainConfigFile = getYmlResource("application")
        val activate = getActivateBydir(mainConfigFile.absolutePath)

        // 查找激活的配置文件，支持.yml和.yaml扩展名
        var activeConfigFile = getYmlResource("application-$activate")
        if (activeConfigFile.exists().not()) {
            activeConfigFile = mainConfigFile
        }
        val loadYmlConfigMap = YmlUtil.loadYmlConfigMap(activeConfigFile.absolutePath)
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