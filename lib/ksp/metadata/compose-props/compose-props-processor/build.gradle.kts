plugins {
    id("site.addzero.buildlogic.kmp.kmp-ksp")
    alias(libs.plugins.site.addzero.gradle.plugin.processor.buddy)
}

val libs = versionCatalogs.named("libs")

processorBuddy {
    packageName.set("site.addzero.composeprops.processor.context")
    mustMap.set(
        mapOf(
            "COMPOSE_ATTRS_SUFFIX" to "State",
        )
    )
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.findLibrary("com-google-devtools-ksp-symbol-processing-api").get())
            implementation(libs.findLibrary("site-addzero-lsi-ksp").get())
            implementation(libs.findLibrary("site-addzero-compose-props-annotations").get())
        }
    }
}
