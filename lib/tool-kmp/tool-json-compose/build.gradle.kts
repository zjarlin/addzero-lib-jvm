plugins {
    id("site.addzero.buildlogic.kmp.kmp-core")
    id("site.addzero.buildlogic.kmp.kmp-json")
}

val libs = versionCatalogs.named("libs")

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(project(":lib:tool-kmp:tool-json"))
            api(libs.findLibrary("org-jetbrains-compose-ui-ui").get())
            api(libs.findLibrary("org-jetbrains-compose-material3-material3").get())
        }
    }
}
