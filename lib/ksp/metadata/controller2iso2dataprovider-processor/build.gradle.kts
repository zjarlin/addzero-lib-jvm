plugins {
    id("site.addzero.buildlogic.kmp.kmp-ksp-plugin")
    id("site.addzero.gradle.plugin.processor-buddy") version "+"
}
val libs = versionCatalogs.named("libs")

processorBuddy {
    packageName.set("site.addzero.controller2iso2dataprovider.processor.context")
    mustMap.set(
        mapOf(
            "sharedComposeSourceDir" to "",
            "iso2DataProviderPackage" to "site.addzero.generated.forms.dataprovider",
            "apiClientPackageName" to "site.addzero.generated.api",
            "apiClientAggregatorObjectName" to "Apis",
            "isomorphicPackageName" to "site.addzero.generated.isomorphic",
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
