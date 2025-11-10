package site.addzero.lib_adaptor

import cn.hutool.core.io.FileUtil
import site.addzero.inter.MpGeneratorSettings
import java.io.File
import java.util.Properties

class MpGeneratorSettingsImpl : MpGeneratorSettings {
    override val resourcePath: String
        get() {
            val properties = Properties().apply {
                this::class.java.classLoader.getResourceAsStream("generator.properties")?.use {
                    load(it)
                }
            }
            val projectRoot = properties.getProperty("project.root") ?: throw RuntimeException("请配置项目根目录")
            val module = properties.getProperty("project.module") ?: throw RuntimeException("请配置项目模块目录")
            val file = FileUtil.file(projectRoot, module, "src", "main", "resources")
            val absolutePath = file.absolutePath
            return absolutePath
        }
    override val authName: String
        get() = "zjarlin"
    override val velocityContextMap: Map<String, Any>
        get() = mapOf(
            "superControllerClass" to "BaseApi",
            "extBaseApiImport" to "com.zlj.common.core.domain.BaseApi",
            "extExcelApiImport" to "com.zlj.common.core.domain.BaseExcelApi",
            "extExcelImport" to "com.zlj.common.annotation.Excel",
            "extExcelAnnoName" to "Excel",
            "extExcelAnnoPropertyName" to "name",
            "extBaseEntityImport" to "com.zlj.common.core.domain.BaseModel"
        )
    override val urlYmlPath: String
        get() = "spring.datasource.druid.master.url"
    override val usernameYmlPath: String
        get() = "spring.datasource.druid.master.username"
    override val passwordYmlPath: String
        get() = "spring.datasource.druid.master.password"
}

