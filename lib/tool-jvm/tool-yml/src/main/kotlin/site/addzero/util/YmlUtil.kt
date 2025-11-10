package site.addzero.util

import org.yaml.snakeyaml.Yaml
import java.io.File
import kotlin.io.path.Path

object YmlUtil {
    private val yaml = Yaml()


    fun String?.replaceEnvInString(defaultValueIfNull: String = ""): String {
        this ?: return defaultValueIfNull
        val regex = Regex("\\$\\{(\\w+)(?::([^}]*))?\\}")
        val replace = regex.replace(this) { matchResult ->
            val (envVar, defaultValue) = matchResult.destructured
            val getenv = getP(envVar)
            println("环境变量${envVar}拿到的值为: $getenv, 默认值为: $defaultValue")
            getenv ?: defaultValue
        }
        return replace
    }


    // 或者更简洁的写法
    fun getP(env: String): String? {
        return listOf(System.getenv(env), System.getProperty(env))
            .firstNotNullOfOrNull { it?.takeIf(String::isNotBlank) }
    }


    fun <T> loadYmlConfig(dir: String): T {
        val inputStream = File(dir).inputStream()
        val config = yaml.load<T>(inputStream)
        return config
    }

    fun loadYmlConfigMap(dir: String): Map<String, Any> {
        val bool = loadYmlConfig<Map<String, Any>>(dir)
        return bool
    }

    fun getActivateBydir(dir: String): String {
        val loadYmlConfigMap = loadYmlConfigMap(dir)
        val configValue = getConfigValue<String>(loadYmlConfigMap, "spring.profiles.active")

        return configValue ?: "dev"
    }


    fun <T> getConfigValue(config: Map<String, Any>, path: String): T? {
        val keys = path.split(".")
        var current: Any? = config

        for (key in keys) {
            current = when (current) {
                is Map<*, *> -> current[key]
                else -> return null
            }
        }

        @Suppress("UNCHECKED_CAST")
        return current as? T
    }


}
