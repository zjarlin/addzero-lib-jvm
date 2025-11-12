package site.addzero.lib_adaptor

import cn.hutool.core.io.FileUtil
import site.addzero.inter.MpGeneratorSettings
import site.addzero.util.PropertyUtil

class MpGeneratorSettingsImpl : MpGeneratorSettings {
    override val resourcePath: String
        get() {
            val projectRoot = PropertyUtil.getProperty("generator.properties", "project.root")
            val module = PropertyUtil.getProperty("generator.properties", "project.yml.module")
            val file = FileUtil.file(projectRoot, module, "src", "main", "resources")
            return file.absolutePath
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
