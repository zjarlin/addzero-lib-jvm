@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    id("site.addzero.buildlogic.kmp.kmp-ksp-plugin")
    id("site.addzero.gradle.plugin.processor-buddy") version "+"
}
val catalogLibs = versionCatalogs.named("libs")

processorBuddy {
    packageName.set("site.addzero.jdbc2enum.processor.context")
    mustMap.set(
        mapOf<String, String>(
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
    )
    readmeEnabled.set(false)
}
kotlin {
  sourceSets.commonMain {
        kotlin.srcDir("build/generated/processor-buddy")
    }
}
kotlin {
    dependencies {
        implementation(catalogLibs.findLibrary("com-google-devtools-ksp-symbol-processing-api").get())
        implementation(project(":lib:ksp:common:ksp-support-jdbc"))
        implementation(catalogLibs.findLibrary("site-addzero-tool-pinyin").get())
        implementation(catalogLibs.findLibrary("site-addzero-tool-str").get())
    }
}
