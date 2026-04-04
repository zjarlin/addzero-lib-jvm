plugins {
    id("site.addzero.buildlogic.kmp.kmp-ksp-plugin")
    alias(libs.plugins.site.addzero.gradle.plugin.processor.buddy)
}
val libs = versionCatalogs.named("libs")

processorBuddy {
    packageName.set("site.addzero.enumprocessor.context")
    mustMap.set(
        mapOf(
            "enumOutputPackage" to "site.addzero.generated.enum",
        )
    )
}

kotlin {

    sourceSets {
        commonMain.dependencies {
            implementation(libs.findLibrary("com-google-devtools-ksp-symbol-processing-api").get())
        }
        jvmMain.dependencies {
//            implementation(libs.findLibrary("com-squareup-kotlinpoet").get())
            implementation(libs.findLibrary("com-squareup-kotlinpoet-ksp").get())
        }
    }
}
