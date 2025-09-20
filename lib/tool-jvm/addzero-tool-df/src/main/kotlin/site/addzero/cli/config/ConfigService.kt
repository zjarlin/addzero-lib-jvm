package site.addzero.cli.config

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.koin.core.annotation.Single
import org.koin.java.KoinJavaComponent.inject
import site.addzero.cli.platform.PlatformService
import site.addzero.cli.platform.PlatformType.*
import site.addzero.cli.setting.SettingContext.CONFIG_FILE
import site.addzero.cli.setting.SettingContext.WORK_DIR
import site.addzero.core.ext.parseObjectByKtx
import site.addzero.core.ext.toJsonByKtx
import site.addzero.ioc.annotation.Bean
import java.io.File

val configService: ConfigService by inject(ConfigService::class.java)

/**
 * 配置管理器
 */
@Single(createdAtStart = true)
class ConfigService {
    var config: Config = loadConfig()
    val osConfig: PlatformConfig = config.getCurrentPlatformConfig()
    fun loadConfig(): Config {

        val file = File(CONFIG_FILE)
        if (!file.exists()) {
            System.err.println("配置文件不存在,开始初始化文件")
            val config1 = Config()
            val toJsonByKtx = config1.toJsonByKtx()
            file.writeText(toJsonByKtx)
            config = config1
            return config

        }
        println("检测到配置文件已存,开始读取最新配置...")
        val parseObjectByKtx = file.readText().parseObjectByKtx<Config>()
        config = parseObjectByKtx
        return parseObjectByKtx
    }

    /**
     * 更新配置
     */
    suspend fun updateConfig(newConfig: Config) {
        config = newConfig
        saveConfig()
    }

    suspend fun updatePlatFormConfig(platformConfig: PlatformConfig) {
        config = when (PlatformService.getPlatformType()) {
            WINDOWS -> config.copy(windowsConfig = platformConfig)
            MACOS -> config.copy(macConfig = platformConfig)
            LINUX -> config.copy(linuxConfig = platformConfig)
            UNKNOWN -> config.copy(linuxConfig = platformConfig)
        }
        saveConfig()
    }


    /**
     * 保存配置
     */
    suspend fun saveConfig() = withContext(Dispatchers.IO) {
        try {
            val file = File(CONFIG_FILE)
            val content = config.toJsonByKtx()
            file.writeText(content)
            true
        } catch (e: Exception) {
            println("保存配置失败: ${e.message}")
            false
        }
    }

    /**
     * 设置dotfiles目录
     */
    suspend fun setDotfilesDir(dir: String) = withContext(Dispatchers.IO) {
        config = config.copy(syncDir = dir)
        saveConfig()
    }


    suspend fun addPkg(packageName: String): Boolean = withContext(
        Dispatchers.IO
    ) {
//        saveConfig()
        val defaultPackages = osConfig.defaultPackages
        val copy = osConfig.copy(defaultPackages = defaultPackages + packageName)
        updatePlatFormConfig(copy)
        saveConfig()
    }


    suspend fun rmPkg(packageName: String): Boolean = withContext(
        Dispatchers.IO
    ) {
//        saveConfig()
        val defaultPackages = osConfig.defaultPackages
        val copy = osConfig.copy(defaultPackages = defaultPackages - packageName)
        updatePlatFormConfig(copy)
        saveConfig()
    }


    /**
     * 设置包管理器
     */
    suspend fun setPackageManager(packageManager: String?) = withContext(Dispatchers.IO) {
        val copy = osConfig.copy(packageManager = packageManager)
        updatePlatFormConfig(copy)
        saveConfig()
    }

    init {

        @Bean
        fun initConfigDir() {
            println("初始化工作目录...")
            val dir = File(WORK_DIR)
            if (!dir.exists()) {
                dir.mkdirs()
            }
        }
        initConfigDir()


        /**
         * 加载配置
         */
        suspend fun loadConfig() = withContext(Dispatchers.IO) {
            val file = File(CONFIG_FILE)
            if (file.exists()) {
                println("目录${CONFIG_FILE}已发现配置文件...")
                try {
                    val content = file.readText()
                    config = content.parseObjectByKtx<Config>()
                    true
                } catch (e: Exception) {
                    println("加载配置失败: ${e.message}")
                    false
                }
            } else {
                // 文件不存在，初始化默认配置
                println("在目录${CONFIG_FILE}初始化配置文件...")
                saveConfig()
            }
        }
        runBlocking {
            loadConfig()
        }

    }
}
