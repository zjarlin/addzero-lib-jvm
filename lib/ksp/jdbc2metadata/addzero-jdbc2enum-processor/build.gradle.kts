@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import com.android.build.gradle.internal.ide.dependencies.getProjectBuildTreePath
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import site.addzero.gradle.plugin.kspbuddy.SettingContextConfig

plugins {
    id("kmp-ksp")
    id("site.addzero.ksp-buddy") version "0.0.608"
}
val properties1 = properties
println("tttt$properties")

kspBuddy {
    // 配置 KSP 参数
    mustMap.set(
        mapOf(
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
    )

    // 配置 KSP 脚本输出路径（默认值,不配置就是这个）
    kspScriptOutputDir = "build-logic/src/main/kotlin/convention-plugins/generated"

    // 配置 SettingContext 生成
    settingContext = SettingContextConfig(
//                （默认值,不配置就是这个）

        contextClassName = "SettingContext",
//                （默认值,不配置就是这个）
        settingsClassName = "Settings",
//                （默认值,不配置就是这个）
        packageName = "site.addzero.context",
//                （默认jvm项目src/main/kotlin,这里举个kmp的例子）
        outputDir = "src/commonMain/kotlin",
        enabled = true
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
