plugins {
    id("site.addzero.buildlogic.kmp.kmp-ksp-plugin")
    id("site.addzero.gradle.plugin.processor-buddy") version "+"
}
val libs = versionCatalogs.named("libs")

processorBuddy {
    packageName.set("site.addzero.jdbc2controller.processor.context")
    mustMap.set(
        mapOf(
            "backendServerSourceDir" to "",
            "controllerOutPackage" to "site.addzero.web.modules.controller",
        )
    )
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.findLibrary("com-google-devtools-ksp-symbol-processing-api").get())
            implementation(project(":lib:ksp:common:ksp-support-jdbc"))
            implementation(libs.findLibrary("site-addzero-tool-jdbc-model").get())
            implementation(libs.findLibrary("site-addzero-tool-str").get())
        }
    }
}
