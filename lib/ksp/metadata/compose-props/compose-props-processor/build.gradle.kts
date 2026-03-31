plugins {
    id("site.addzero.buildlogic.kmp.kmp-ksp")
}

val libs = versionCatalogs.named("libs")

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.findLibrary("com-google-devtools-ksp-symbol-processing-api").get())
            implementation(project(":lib:lsi:lsi-ksp"))
            implementation("site.addzero:compose-props-annotations:2025.09.30")
        }
    }
}
