plugins {
    id("site.addzero.buildlogic.kmp.kmp-ksp-plugin")
    id("site.addzero.gradle.plugin.processor-buddy") version "+"
}
val libs = versionCatalogs.named("libs")

processorBuddy {
    packageName.set("site.addzero.entity2form.processor.context")
    mustMap.set(
        mapOf(
            "sharedComposeSourceDir" to "",
            "formPackageName" to "site.addzero.generated.forms",
            "iso2DataProviderPackage" to "site.addzero.generated.forms.dataprovider",
            "isomorphicPackageName" to "site.addzero.generated.isomorphic",
            "enumOutputPackage" to "site.addzero.generated.enums",
        )
    )
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.findLibrary("androidx-room-compiler-processing").get())
            implementation(libs.findLibrary("site-addzero-entity2form-core").get())
            implementation(libs.findLibrary("site-addzero-tool-str").get())
            implementation(project(":lib:ksp:metadata:jimmer-entity-spi"))
        }
    }
}
