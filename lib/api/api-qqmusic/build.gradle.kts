plugins {
    id("site.addzero.buildlogic.kmp.kmp-ktorfit")
    id("site.addzero.buildlogic.kmp.kmp-ktor-client")
    id("site.addzero.buildlogic.kmp.kmp-json-withtool")

}
val libs = versionCatalogs.named("libs")

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":lib:tool-kmp:network-starter"))
//            api(projects.lib.apiMusicSpi)
        }
        commonTest.dependencies {
            implementation(libs.findLibrary("org-jetbrains-kotlinx-kotlinx-coroutines-test").get())
        }

    }
}
