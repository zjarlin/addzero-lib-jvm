plugins {
    id("site.addzero.buildlogic.kmp.kmp-ksp-plugin")
    alias(libs.plugins.site.addzero.gradle.plugin.processor.buddy)
}
val libs = versionCatalogs.named("libs")

processorBuddy {
    packageName.set("site.addzero.controller2feign.processor.context")
    mustMap.set(
        mapOf(
            "feignOutputPackage" to "site.addzero.generated.feign",
            "feignOutputDir" to "",
            "feignEnabled" to "true",
        )
    )
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.findLibrary("com-google-devtools-ksp-symbol-processing-api").get())
        }
    }
}
