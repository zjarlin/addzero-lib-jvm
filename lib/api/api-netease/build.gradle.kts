plugins {
    id("site.addzero.buildlogic.kmp.kmp-ktorfit")
    id("site.addzero.buildlogic.kmp.kmp-koin-core")
    id("site.addzero.buildlogic.kmp.kmp-json-withtool")
    id("site.addzero.buildlogic.kmp.kmp-ksp-plugin")
}

val libs = versionCatalogs.named("libs")

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":lib:tool-kmp:network-starter"))
        }
    }
}
