@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    id("site.addzero.buildlogic.kmp.kmp-ksp")
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

    // 配置 KSP 脚本输出路径（默认值,不配置就是这个）
    val string = "checkouts/build-logic-klibs/src/main/kotlin/site/addzero/buildlogic/generated"
    kspScriptOutputDir = string
}
kotlin {
    sourceSets.commonMain {
        kotlin.srcDir("build/generated/ksp-buddy")
    }
}
kotlin {
    dependencies {
        implementation(libs.site.addzero.addzero.ksp.support.jdbc)
        implementation(libs.site.addzero.addzero.ksp.support)
    }
}
