plugins {
    id("site.addzero.buildlogic.kmp.kmp-ksp-plugin")
    id("site.addzero.gradle.plugin.processor-buddy") version "+"
}
val libs = versionCatalogs.named("libs")

processorBuddy {
    packageName.set("site.addzero.jdbc2entity.processor.context")
    mustMap.set(
        mapOf(
            "baseEntityPackage" to "",
            "id" to "",
            "createBy" to "",
            "updateBy" to "",
            "createTime" to "",
            "updateTime" to "",
            "backendModelSourceDir" to "",
            "modelPackageName" to "",
        )
    )
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.findLibrary("com-google-devtools-ksp-symbol-processing-api").get())
            implementation(libs.findLibrary("site-addzero-ksp-support-jdbc").get())
            implementation(libs.findLibrary("site-addzero-tool-str").get())
        }
    }
}
