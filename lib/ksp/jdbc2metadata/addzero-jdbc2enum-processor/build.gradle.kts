@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    id("kmp-ksp")
    alias(libs.plugins.addzeroKspBuddy)
}

// 配置 KSP 参数

kspBuddy {
    mustMap = mapOf<String, String>(
        "enumOutputPackage" to "site.addzero.generated.enums",
        "dictTableName" to "sys_dict",
        "dictIdColumn" to "id",
        "dictCodeColumn" to "dict_code",
        "dictNameColumn" to "dict_name",
        "dictItemTableName" to "sys_dict_item",
        "dictItemForeignKeyColumn" to "dict_id",
        "dictItemCodeColumn" to "item_value",
        "dictItemNameColumn" to "item_text",
        "sharedSourceDir" to ""
    )


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
