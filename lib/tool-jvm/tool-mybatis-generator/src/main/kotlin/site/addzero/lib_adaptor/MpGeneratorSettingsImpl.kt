package site.addzero.lib_adaptor

import site.addzero.inter.MpGeneratorSettings

class MpGeneratorSettingsImpl : MpGeneratorSettings {
    override val resourcePath: String
        get() = "C:\\Users\\wang\\IdeaProjects\\producttrace-master\\zlj-admin\\src\\main\\resources"
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

