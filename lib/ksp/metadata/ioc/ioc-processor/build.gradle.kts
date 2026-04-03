plugins {
    id("site.addzero.buildlogic.kmp.kmp-ksp-plugin")
    alias(libs.plugins.site.addzero.gradle.plugin.processor.buddy)
}
val libs = versionCatalogs.named("libs")

processorBuddy {
    packageName.set("site.addzero.ioc.processor.context")
    mustMap.set(
        mapOf(
            "ioc.module" to "",
            "ioc.role" to "lib",
        )
    )
}

kotlin {

    sourceSets {
        commonMain.dependencies {
            implementation(libs.findLibrary("com-google-devtools-ksp-symbol-processing-api").get())
            implementation(libs.findLibrary("site-addzero-ioc-core").get())
            implementation(libs.findLibrary("site-addzero-lsi-ksp").get())
        }
    }
}

//version="2026.02.18"
