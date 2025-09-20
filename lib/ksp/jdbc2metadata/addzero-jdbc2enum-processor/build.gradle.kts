@file:OptIn(ExperimentalKotlinGradlePluginApi::class)
//import site.addzero.gradle.tool.getProjectDirConfigMap
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import site.addzero.gradle.tool.getProjectDirConfigMapWithOutLib

plugins {
    id("kmp-ksp")
    alias(libs.plugins.addzeroKspBuddy)
}
val projectDirConfigMap = getProjectDirConfigMapWithOutLib()

// 配置 KSP 参数
val entries = mapOf(
    "enumOutputPackage" to "site.addzero.generated.enums",
    "dictTableName" to "sys_dict",
    "dictIdColumn" to "id",
    "dictCodeColumn" to "dict_code",
    "dictNameColumn" to "dict_name",
    "dictItemTableName" to "sys_dict_item",
    "dictItemForeignKeyColumn" to "dict_id",
    "dictItemCodeColumn" to "item_value",
    "dictItemNameColumn" to "item_text",
)
projectDirConfigMap.putAll(entries)

kspBuddy {
    mustMap.set(
        projectDirConfigMap
    )

//    settingContext = SettingContextConfig(outputDir = "src/commonMain/kotlin")

}
kotlin {
    sourceSets.commonMain {
        kotlin.srcDir("build/generated/ksp-buddy")
    }
}

kotlin {
    dependencies {
        implementation(projects.lib.ksp.common.addzeroKspSupportJdbc)
        implementation(projects.lib.ksp.common.addzeroKspSupport)
    }
}
